package com.koner.typst

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.rk.extension.ExtensionAPI
import com.rk.extension.ExtensionContext
import com.rk.file.FileTypeManager
import com.rk.file.child
import com.rk.file.createDirIfNot
import com.rk.file.localBinDir
import com.rk.icons.Icon
import com.rk.lsp.LspRegistry
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlin.io.writeText

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {
    private var fileResolver: AssetsFileResolver? = null
    private var typstLanguage: TypstLanguage? = null
    private var typstServer: TypstServer? = null

    override fun onExtensionLoaded() {
        val fileProviderRegistry = FileProviderRegistry.getInstance()
        fileResolver = AssetsFileResolver(context.assets)
        fileProviderRegistry.addFileProvider(fileResolver)

        val grammarRegistry = GrammarRegistry.getInstance()
        grammarRegistry.loadGrammars("language.json")

        val typstIcon = Icon.ExternalResourceIcon(R.drawable.typst, context.resources)
        typstLanguage = TypstLanguage(typstIcon).also {
            FileTypeManager.register(it)
        }

        // Copy script from extension assets to local/bin/lsp directory
        val typstAssetStream = context.assets.open("typst-lsp.sh")
        val typstAsset = typstAssetStream.bufferedReader().use { it.readText() }
        val lspScriptDir = localBinDir().child("lsp").createDirIfNot()
        val typstInstallScript = lspScriptDir.child("typst-lsp.sh").also {
            it.writeText(typstAsset)
        }

        typstServer = TypstServer(typstIcon, typstInstallScript).also {
            LspRegistry.registerServer(it)
        }
    }

    override fun onUninstalled() {
        val fileProviderRegistry = FileProviderRegistry.getInstance()
        fileResolver?.let {
            fileProviderRegistry.removeFileProvider(it)
        }
        typstLanguage?.let {
            FileTypeManager.unregister(it)
        }
        typstServer?.let {
            LspRegistry.unregisterServer(it)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}
}