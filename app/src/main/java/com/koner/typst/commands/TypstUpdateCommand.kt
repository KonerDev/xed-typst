package com.koner.typst.commands

import android.content.res.Resources
import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationAction
import com.koner.typst.utils.TypstInstallationManager
import com.rk.commands.ActionContext
import com.rk.commands.GlobalCommand
import com.rk.extension.ExtensionContext
import com.rk.icons.Icon

class TypstUpdateCommand(
    private val icon: Icon,
    private val resources: Resources,
    private val typstInstallationManager: TypstInstallationManager
) : GlobalCommand() {

    override val id = "typst.cli.update"

    override val prefix = "Typst"

    override fun getLabel() = resources.getString(R.string.update_cli)

    override fun getIcon() = icon

    override fun isEnabled(): Boolean {
        return typstInstallationManager.cachedPendingAction == TypstInstallationAction.UPDATE
    }

    override fun action(actionContext: ActionContext) {
        typstInstallationManager.performAction(TypstInstallationAction.UPDATE)
    }
}
