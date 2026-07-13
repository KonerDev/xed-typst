package com.koner.typst.runner

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationManager
import com.rk.exec.ubuntuProcess
import com.rk.extension.ExtensionContext
import com.rk.file.FileObject
import com.rk.icons.Icon
import com.rk.resources.fillPlaceholders
import com.rk.runner.Runner
import com.rk.utils.toast
import java.net.ServerSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TypstPreviewRunner(
    private val icon: Icon,
    private val context: ExtensionContext,
    private val supportedExtensions: List<String>,
    private val typstInstallationManager: TypstInstallationManager,
    resources: Resources,
) : Runner() {

    companion object {
        private var process: Process? = null
        private var port: Int? = null
        private var previewFile: FileObject? = null
    }

    override val id = "typst.preview"

    override val label = resources.getString(R.string.preview_document)

    override fun getIcon(context: Context) = icon

    override fun matcher(fileObject: FileObject): Boolean {
        return supportedExtensions.contains(fileObject.getExtension())
    }

    override suspend fun run(activity: Activity, fileObject: FileObject) {
        if (!typstInstallationManager.ensureCliInstalled()) return

        if (previewFile == fileObject && isRunning() && port != null) {
            val url = "http://localhost:$port"
            previewUrl(activity, url)
            return
        }

        stop()
        previewFile = fileObject

        val port =
            withContext(Dispatchers.IO) {
                    ServerSocket(0).use { it.localPort }
                }
                .also {
                    port = it
                }
        val url = "http://localhost:$port"
        val workingDir = fileObject.getParentFile()?.getAbsolutePath()

        val process =
            ubuntuProcess(
                    workingDir = workingDir,
                    command =
                        listOf(
                            TypstInstallationManager.TYPST_PATH,
                            "watch",
                            "--format",
                            "html",
                            "--features",
                            "html",
                            "--port",
                            port.toString(),
                            fileObject.getName(),
                        ),
                )
                .also {
                    process = it
                }

        previewUrl(activity, url)

        context.scope.launch(Dispatchers.IO) {
            try {
                val errors = mutableListOf<String>()

                val errorJob = launch {
                    process.errorStream.bufferedReader().useLines { lines ->
                        lines.forEach { errors.add(it) }
                    }
                }

                val exitCode = process.waitFor()

                errorJob.join()

                if (exitCode != 0) {
                    val error = errors.joinToString("\n")

                    context.logError("Preview error: $error")
                    activity.runOnUiThread {
                        toast(context.resources.getString(R.string.preview_error).fillPlaceholders(error))
                    }
                }
            } finally {
                TypstPreviewRunner.process = null
                TypstPreviewRunner.port = null
                previewFile = null
            }
        }
    }

    private fun previewUrl(activity: Activity, url: String) {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()
            .launchUrl(activity, url.toUri())
    }

    override suspend fun isRunning() = process?.isAlive ?: false

    override suspend fun stop() {
        process?.destroyForcibly()
        process = null
        port = null
        previewFile = null
    }
}
