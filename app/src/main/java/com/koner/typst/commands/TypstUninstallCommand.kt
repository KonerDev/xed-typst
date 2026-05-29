package com.koner.typst.commands

import android.content.res.Resources
import com.koner.typst.R
import com.koner.typst.utils.TypstInstallationAction
import com.koner.typst.utils.TypstInstallationManager
import com.rk.commands.ActionContext
import com.rk.commands.GlobalCommand
import com.rk.icons.Icon

class TypstUninstallCommand(
    private val icon: Icon,
    private val resources: Resources,
    private val typstInstallationManager: TypstInstallationManager
) : GlobalCommand() {

    override val id = "typst.cli.uninstall"

    override val prefix = "Typst"

    override fun getLabel(): String {
        return if (typstInstallationManager.isCliInstalled()) {
            resources.getString(R.string.uninstall_cli)
        } else {
            resources.getString(R.string.install_cli)
        }
    }

    override fun getIcon() = icon

    override fun action(actionContext: ActionContext) {
        if (typstInstallationManager.isCliInstalled()) {
            typstInstallationManager.performAction(TypstInstallationAction.UNINSTALL)
        } else {
            typstInstallationManager.performAction(TypstInstallationAction.INSTALL)
        }
    }
}
