package com.koner.typst.commands.compile

import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationManager
import com.rk.commands.EditorActionContext
import com.rk.commands.EditorCommand
import com.rk.commands.EditorNonActionContext
import com.rk.extension.ExtensionContext
import com.rk.icons.Icon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TypstCompilePdfCommand(
    private val icon: Icon,
    private val context: ExtensionContext,
    private val supportedExtensions: List<String>,
    private val typstInstallationManager: TypstInstallationManager,
) : EditorCommand() {

    override val id = "typst.compile.pdf"

    override val prefix = "Typst"

    override fun getLabel() = context.resources.getString(R.string.compile_document_pdf)

    override fun getIcon(): Icon = icon

    override fun isSupported(editorNonActionContext: EditorNonActionContext): Boolean {
        val fileObject = editorNonActionContext.editorTab.file
        return supportedExtensions.contains(fileObject.getExtension())
    }

    override fun action(editorActionContext: EditorActionContext) {
        if (!typstInstallationManager.ensureCliInstalled()) return

        context.scope.launch(Dispatchers.IO) {
            compile(context, editorActionContext.editorTab.file, "pdf")
        }
    }
}
