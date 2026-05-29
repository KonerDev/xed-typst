package com.koner.typst.runner

import android.content.Context
import android.content.res.Resources
import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationManager
import com.rk.file.FileObject
import com.rk.icons.Icon
import com.rk.runner.Runner

class TypstCompileRunner(
    private val icon: Icon,
    private val supportedExtensions: List<String>,
    private val typstInstallationManager: TypstInstallationManager,
    resources: Resources,
) : Runner() {

    override val id = "typst.compile"

    override val label = resources.getString(R.string.compile_document)

    override fun getIcon(context: Context) = icon

    override fun matcher(fileObject: FileObject): Boolean {
        return supportedExtensions.contains(fileObject.getExtension())
    }

    override suspend fun run(context: Context, fileObject: FileObject) {
        val workingDir = fileObject.getParentFile()?.getAbsolutePath()
        typstInstallationManager.launchTypstCommand(label, workingDir, "compile", fileObject.getName())
    }

    override suspend fun isRunning() = false

    override suspend fun stop() {}
}