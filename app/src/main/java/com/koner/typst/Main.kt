package com.koner.typst

import androidx.annotation.Keep
import com.koner.typst.commands.cli.TypstUninstallCommand
import com.koner.typst.commands.cli.TypstUpdateCommand
import com.koner.typst.commands.compile.TypstCompileHtmlCommand
import com.koner.typst.commands.compile.TypstCompilePdfCommand
import com.koner.typst.commands.compile.TypstCompilePngCommand
import com.koner.typst.commands.compile.TypstCompileSvgCommand
import com.koner.typst.runner.TypstPreviewRunner
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
import kotlinx.coroutines.runBlocking
import java.io.File

@Keep
@Suppress("unused")
class Main(context: ExtensionContext) : ExtensionAPI(context) {
    private var fileResolver: AssetsFileResolver? = null
    private var typstLanguage: TypstLanguage? = null
    private var typstServer: TypstServer? = null

    private var typstPreviewRunner: TypstPreviewRunner? = null

    private var typstCompilePdfCommand: TypstCompilePdfCommand? = null
    private var typstCompileHtmlCommand: TypstCompileHtmlCommand? = null
    private var typstCompilePngCommand: TypstCompilePngCommand? = null
    private var typstCompileSvgCommand: TypstCompileSvgCommand? = null

    private var typstInstallationManager: TypstInstallationManager? = null
    private var typstUninstallCommand: TypstUninstallCommand? = null
    private var typstUpdateCommand: TypstUpdateCommand? = null

    override fun onLoad() {
        val fileProviderRegistry = FileProviderRegistry.getInstance()
        fileResolver = AssetsFileResolver(context.assets)
        fileProviderRegistry.addFileProvider(fileResolver)

        val grammarRegistry = GrammarRegistry.getInstance()
        grammarRegistry.loadGrammars("language.json")

        val fileType =
            TypstLanguage(context.resources).also {
                typstLanguage = it
                FileTypeManager.register(it)
            }

        typstServer =
            TypstServer(
                    context = context,
                    icon = fileType.icon,
                    supportedExtensions = fileType.extensions,
                    installScript = acquireLspInstallScript(),
                )
                .also {
                    LspRegistry.registerServer(it)
                }

        val typstCliScript = acquireCliInstallScript()
        val manager =
            TypstInstallationManager(typstCliScript, context).also {
                typstInstallationManager = it
            }

        manager.performStartupActions()

        typstPreviewRunner =
            TypstPreviewRunner(
                    icon = fileType.icon,
                    context = context,
                    supportedExtensions = fileType.extensions,
                    typstInstallationManager = manager,
                    resources = context.resources,
                )
                .also {
                    RunnerManager.registerRunner(it)
                }

        typstCompilePdfCommand =
            TypstCompilePdfCommand(
                    icon = fileType.icon,
                    context = context,
                    supportedExtensions = fileType.extensions,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }

        typstCompileHtmlCommand =
            TypstCompileHtmlCommand(
                    icon = fileType.icon,
                    context = context,
                    supportedExtensions = fileType.extensions,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }

        typstCompilePngCommand =
            TypstCompilePngCommand(
                    icon = fileType.icon,
                    context = context,
                    supportedExtensions = fileType.extensions,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }

        typstCompileSvgCommand =
            TypstCompileSvgCommand(
                    icon = fileType.icon,
                    context = context,
                    supportedExtensions = fileType.extensions,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }

        typstUninstallCommand =
            TypstUninstallCommand(
                    icon = fileType.icon,
                    resources = context.resources,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }

        typstUpdateCommand =
            TypstUpdateCommand(
                    icon = fileType.icon,
                    resources = context.resources,
                    typstInstallationManager = manager,
                )
                .also {
                    CommandProvider.registerCommand(it)
                }
    }

    private fun acquireLspInstallScript(): File {
        val typstAssetStream = context.assets.open("typst-lsp.sh")
        val typstAsset = typstAssetStream.bufferedReader().use { it.readText() }
        val typstLspScript =
            getTempDir().child("typst-lsp.sh").also {
                it.writeText(typstAsset)
            }
        return typstLspScript
    }

    private fun acquireCliInstallScript(): File {
        val typstAssetStream = context.assets.open("typst-cli.sh")
        val typstAsset = typstAssetStream.bufferedReader().use { it.readText() }
        val typstCliScript =
            getTempDir().child("typst-cli.sh").also {
                it.writeText(typstAsset)
            }
        return typstCliScript
    }

    override fun onDispose() {
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

        typstPreviewRunner?.let {
            RunnerManager.unregisterRunner(it)
        }

        typstCompilePdfCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstCompileHtmlCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstCompilePngCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstCompileSvgCommand?.let {
            CommandProvider.unregisterCommand(it)
        }

        typstUninstallCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
        typstUpdateCommand?.let {
            CommandProvider.unregisterCommand(it)
        }
    }

    override fun onUninstalled() {
        context.currentActivity?.let {
            val isInstalled = runBlocking { typstServer?.isInstalled(it) } ?: false
            if (isInstalled) {
                typstServer?.uninstall(it)
            }
        }
        typstInstallationManager?.onUninstalled()
    }
}
