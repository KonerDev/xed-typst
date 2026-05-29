package com.koner.typst

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.koner.typst.commands.TypstUninstallCommand
import com.koner.typst.commands.TypstUpdateCommand
import com.koner.typst.runner.TypstCompileRunner
import com.koner.typst.runner.TypstWatchRunner
import com.koner.typst.utils.TypstInstallationManager
import com.rk.commands.CommandProvider
import com.rk.extension.ExtensionAPI
import com.rk.extension.ExtensionContext
import com.rk.file.FileTypeManager
import com.rk.file.child
import com.rk.lsp.LspRegistry
import com.rk.runner.RunnerManager
import com.rk.utils.getTempDir
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.writeText

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {
    private var fileResolver: AssetsFileResolver? = null
    private var typstLanguage: TypstLanguage? = null
    private var typstServer: TypstServer? = null
    private var typstCompileRunner: TypstCompileRunner? = null
    private var typstWatchRunner: TypstWatchRunner? = null
    private var typstInstallationManager: TypstInstallationManager? = null
    private var typstUninstallCommand: TypstUninstallCommand? = null
    private var typstUpdateCommand: TypstUpdateCommand? = null

    override fun onExtensionLoaded() {
        val fileProviderRegistry = FileProviderRegistry.getInstance()
        fileResolver = AssetsFileResolver(context.assets)
        fileProviderRegistry.addFileProvider(fileResolver)

        val grammarRegistry = GrammarRegistry.getInstance()
        grammarRegistry.loadGrammars("language.json")

        val fileType = TypstLanguage(context.resources).also {
            typstLanguage = it
            FileTypeManager.register(it)
        }

        typstServer = TypstServer(
            icon = fileType.icon,
            supportedExtensions = fileType.extensions,
            installScript = acquireLspInstallScript()
        ).also {
            LspRegistry.registerServer(it)
        }

        val typstCliScript = acquireCliInstallScript()
        val manager = TypstInstallationManager(typstCliScript, context).also {
            typstInstallationManager = it
        }

        context.scope.launch(Dispatchers.IO) {
            manager.performStartupActions()
        }

        typstCompileRunner = TypstCompileRunner(
            icon = fileType.icon,
            supportedExtensions = fileType.extensions,
            typstInstallationManager = manager,
            resources = context.resources,
        ).also {
            RunnerManager.registerRunner(it)
        }

        typstWatchRunner = TypstWatchRunner(
            icon = fileType.icon,
            supportedExtensions = fileType.extensions,
            typstInstallationManager = manager,
            resources = context.resources,
        ).also {
            RunnerManager.registerRunner(it)
        }

        typstUninstallCommand = TypstUninstallCommand(
            icon = fileType.icon,
            resources = context.resources,
            typstInstallationManager = manager,
        ).also {
            CommandProvider.registerCommand(it)
        }

        typstUpdateCommand = TypstUpdateCommand(
            icon = fileType.icon,
            resources = context.resources,
            typstInstallationManager = manager,
        ).also {
            CommandProvider.registerCommand(it)
        }
    }

    private fun acquireLspInstallScript(): File {
        val typstAssetStream = context.assets.open("typst-lsp.sh")
        val typstAsset = typstAssetStream.bufferedReader().use { it.readText() }
        val typstLspScript = getTempDir().child("typst-lsp.sh").also {
            it.writeText(typstAsset)
        }
        return typstLspScript
    }

    private fun acquireCliInstallScript(): File {
        val typstAssetStream = context.assets.open("typst-cli.sh")
        val typstAsset = typstAssetStream.bufferedReader().use { it.readText() }
        val typstCliScript = getTempDir().child("typst-cli.sh").also {
            it.writeText(typstAsset)
        }
        return typstCliScript
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
        typstCompileRunner?.let {
            RunnerManager.unregisterRunner(it)
        }
        typstWatchRunner?.let {
            RunnerManager.unregisterRunner(it)
        }
        typstUninstallCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstUpdateCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstInstallationManager?.onUninstalled()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}
}