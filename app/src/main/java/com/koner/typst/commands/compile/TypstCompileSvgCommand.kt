package com.koner.typst.commands.compile

import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationManager
import com.rk.commands.EditorFileActionContext
import com.rk.commands.EditorFileCommand
import com.rk.commands.EditorFileNonActionContext
import com.rk.extension.ExtensionContext
import com.rk.icons.Icon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TypstCompileSvgCommand(
    private val icon: Icon,
    private val extensionContext: ExtensionContext,
    private val supportedExtensions: List<String>,
    private val typstInstallationManager: TypstInstallationManager,
) : EditorFileCommand() {

    override val id = "typst.compile.svg"

    override val prefix = "Typst"

    override fun getLabel() = extensionContext.resources.getString(R.string.compile_document_svg)

    override fun getIcon(): Icon = icon

    override fun isSupported(context: EditorFileNonActionContext): Boolean {
        return supportedExtensions.contains(context.file.getExtension())
    }

    override fun execute(context: EditorFileActionContext) {
        if (!typstInstallationManager.ensureCliInstalled()) return

        extensionContext.scope.launch(Dispatchers.IO) {
            compile(extensionContext, context.file, "svg")
        }
    }
}
