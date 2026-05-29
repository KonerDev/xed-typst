package com.koner.typst

import android.content.Context
import com.rk.exec.isTerminalInstalled
import com.rk.file.child
import com.rk.file.sandboxHomeDir
import com.rk.icons.Icon
import com.rk.lsp.LspConnectionConfig
import com.rk.lsp.ScriptedLspServer
import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull
import java.io.File

class TypstServer(
    override val icon: Icon,
    override val supportedExtensions: List<String>,
    override val installScript: File,
) : ScriptedLspServer() {

    override val id = "typst"
    override val languageName = "Typst"
    override val serverName = "tinymist"

    override val installId = "Tinymist language server"

    companion object {
        // Has to be manually updated when a new version is released (Don't forgot to also update typst-lsp.sh)
        private const val LATEST_VERSION = "v0.14.18"
    }

    override suspend fun isInstalled(context: Context): Boolean {
        if (!isTerminalInstalled()) {
            return false
        }

        return sandboxHomeDir().child(".lsp/typst/tinymist").exists()
    }

    override suspend fun isUpdatable(context: Context): Boolean {
        val versionFile = sandboxHomeDir().child(".lsp/typst/version.txt")
        val current = runCatching { versionFile.readText().trim() }.getOrNull()
        val currentVersion = current?.toVersionOrNull(false) ?: return false
        return currentVersion < LATEST_VERSION.toVersion(false)
    }

    override fun getConnectionConfig(): LspConnectionConfig {
        return LspConnectionConfig.Process(arrayOf("/home/.lsp/typst/tinymist"))
    }
}