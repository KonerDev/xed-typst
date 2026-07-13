package com.koner.typst

import android.app.Activity
import android.content.Context
import com.koner.typst.utils.GithubReleasesApi
import com.rk.exec.isTerminalInstalled
import com.rk.extension.ExtensionContext
import com.rk.file.child
import com.rk.file.sandboxHomeDir
import com.rk.icons.Icon
import com.rk.lsp.LspConnectionConfig
import com.rk.lsp.ScriptedLspServer
import io.github.z4kn4fein.semver.toVersionOrNull
import kotlinx.coroutines.launch
import java.io.File

class TypstServer(
    private val context: ExtensionContext,
    override val icon: Icon,
    override val supportedExtensions: List<String>,
    override val installScript: File,
) : ScriptedLspServer() {

    override val id = "typst"
    override val languageName = "Typst"
    override val serverName = "tinymist"

    override val installId = "Tinymist language server"

    private suspend fun fetchLatestVersion(): String {
        return GithubReleasesApi("Myriad-Dreamin", "tinymist").fetchLatestVersion() ?: "v0.15.2"
    }

    override suspend fun isInstalled(context: Context): Boolean {
        if (!isTerminalInstalled()) {
            return false
        }

        return sandboxHomeDir().child(".lsp/typst/tinymist").exists()
    }

    override fun install(activity: Activity) {
        context.scope.launch {
            launchInstaller(activity, "--install", fetchLatestVersion())
        }
    }

    override fun uninstall(activity: Activity) {
        context.scope.launch {
            launchInstaller(activity, "--uninstall", fetchLatestVersion())
        }
    }

    override fun update(activity: Activity) {
        context.scope.launch {
            launchInstaller(activity, "--update", fetchLatestVersion())
        }
    }

    override suspend fun hasUpdate(context: Context): Boolean {
        val versionFile = sandboxHomeDir().child(".lsp/typst/version.txt")
        val currentVersionText = runCatching { versionFile.readText().trim() }.getOrNull()
        val currentVersion = currentVersionText?.toVersionOrNull(false) ?: return false
        val latestVersion = fetchLatestVersion().toVersionOrNull(false) ?: return false
        return currentVersion < latestVersion
    }

    override fun getConnectionConfig(): LspConnectionConfig {
        return LspConnectionConfig.Process(arrayOf("/home/.lsp/typst/tinymist"))
    }
}
