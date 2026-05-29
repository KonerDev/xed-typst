package com.koner.typst

import android.content.Context
import com.koner.typst.utils.GithubReleasesApi
import com.rk.exec.isTerminalInstalled
import com.rk.file.child
import com.rk.file.sandboxHomeDir
import com.rk.icons.Icon
import com.rk.lsp.LspConnectionConfig
import com.rk.lsp.ScriptedLspServer
import io.github.z4kn4fein.semver.toVersionOrNull
import java.io.File

class TypstServer(
    override val icon: Icon,
    override val supportedExtensions: List<String>,
    override val installScript: File
) : ScriptedLspServer() {

    override val id = "typst"
    override val languageName = "Typst"
    override val serverName = "tinymist"

    override val installId = "Tinymist language server"

    val latestVersion by lazy {
        GithubReleasesApi("Myriad-Dreamin", "tinymist").fetchLatestVersion() ?: "v0.14.18"
    }

    override suspend fun isInstalled(context: Context): Boolean {
        if (!isTerminalInstalled()) {
            return false
        }

        return sandboxHomeDir().child(".lsp/typst/tinymist").exists()
    }

    override fun install(context: Context) = launchInstaller(context, latestVersion)

    override fun uninstall(context: Context) = launchInstaller(context, "--uninstall", latestVersion)

    override fun update(context: Context) = launchInstaller(context, "--update", latestVersion)

    override suspend fun isUpdatable(context: Context): Boolean {
        val versionFile = sandboxHomeDir().child(".lsp/typst/version.txt")
        val currentVersionText = runCatching { versionFile.readText().trim() }.getOrNull()
        val currentVersion = currentVersionText?.toVersionOrNull(false) ?: return false
        val latestVersion = latestVersion.toVersionOrNull(false) ?: return false
        return currentVersion < latestVersion
    }

    override fun getConnectionConfig(): LspConnectionConfig {
        return LspConnectionConfig.Process(arrayOf("/home/.lsp/typst/tinymist"))
    }
}