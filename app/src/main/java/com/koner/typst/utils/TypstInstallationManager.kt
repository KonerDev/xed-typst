package com.koner.typst.utils

import com.koner.typst.R
import com.rk.activities.main.MainActivity
import com.rk.exec.ShellUtils
import com.rk.exec.TerminalCommand
import com.rk.exec.launchTerminal
import com.rk.extension.ExtensionContext
import com.rk.file.child
import com.rk.file.sandboxHomeDir
import com.rk.utils.dialog
import com.rk.utils.toast
import io.github.z4kn4fein.semver.toVersionOrNull
import java.io.File

enum class TypstInstallationAction {
    INSTALL, UPDATE, UNINSTALL
}

data class TypstInstallationManager(
    private val script: File,
    private val context: ExtensionContext
) {

    var cachedPendingAction: TypstInstallationAction? = null
        private set

    suspend fun performStartupActions() {
        val pendingAction = checkForAction()

//        if (pendingAction == TypstInstallationAction.INSTALL) {
//            showInstallDialog { manageInstallation(pendingAction) }
//            return
//        }

        if (pendingAction == TypstInstallationAction.UPDATE) {
            showUpdateDialog { manageInstallation(pendingAction) }
        }
    }

    fun performAction(action: TypstInstallationAction) {
        when (action) {
            TypstInstallationAction.INSTALL -> showInstallDialog {
                manageInstallation(action)
            }
            TypstInstallationAction.UPDATE -> showUpdateDialog {
                manageInstallation(action)
            }
            TypstInstallationAction.UNINSTALL -> showUninstallConfirmDialog {
                manageInstallation(action)
            }
        }
    }

    fun onUninstalled() {
        if (!isCliInstalled()) return

        showUninstallQuestionDialog {
            manageInstallation(TypstInstallationAction.UNINSTALL)
        }
    }

    suspend fun checkForAction(): TypstInstallationAction? {
        return when {
            !isCliInstalled() -> TypstInstallationAction.INSTALL
            isUpdateAvailable() -> TypstInstallationAction.UPDATE
            else -> null
        }.also {
            cachedPendingAction = it
        }
    }

    fun isCliInstalled(): Boolean {
        return sandboxHomeDir().child(".local/bin/typst").exists()
    }

    private suspend fun isUpdateAvailable(): Boolean {
        if (!isCliInstalled()) return false
        val currentVersion = getInstalledVersion()?.toVersionOrNull(false) ?: return false
        val latestVersion = fetchLatestVersion()?.toVersionOrNull(false) ?: return false

        return currentVersion < latestVersion
    }

    private suspend fun getInstalledVersion(): String? {
        val result =
            ShellUtils.runUbuntu(
                command = arrayOf("/home/.local/bin/typst", "--version"),
                timeoutSeconds = 20L,
            )
        if (result.timedOut || result.exitCode != 0) return null

        return runCatching {
            Regex("""\d+\.\d+\.\d+""")
                .find(result.output)
                ?.value
        }
            .getOrNull()
    }

    private fun fetchLatestVersion() = GithubReleasesApi("typst", "typst").fetchLatestVersion()

    private fun manageInstallation(action: TypstInstallationAction) {
        val flag = when (action) {
            TypstInstallationAction.INSTALL -> "--install"
            TypstInstallationAction.UPDATE -> "--update"
            TypstInstallationAction.UNINSTALL -> "--uninstall"
        }

        val activity = MainActivity.instance ?: return

        launchTerminal(
            context = activity,
            terminalCommand = TerminalCommand(
                exe = "/bin/bash",
                args = arrayOf(script.absolutePath, flag),
                id = "Typst installation",
                env = arrayOf("DEBIAN_FRONTEND=noninteractive"),
            ),
        )
    }

    fun launchTypstCommand(id: String, workingDir: String?, vararg command: String) {
        if (!isCliInstalled()) {
            toast(context.resources.getString(R.string.cli_not_installed))
            performAction(TypstInstallationAction.INSTALL)
            return
        }

        val activity = MainActivity.instance ?: return

        launchTerminal(
            context = activity,
            terminalCommand = TerminalCommand(
                exe = "/home/.local/bin/typst",
                args = arrayOf(*command),
                id = id,
                workingDir = workingDir,
            ),
        )
    }

    private fun showInstallDialog(onConfirm: () -> Unit) {
        val installLabel = context.appResources.getString("install") ?: "Install"
        val activity = MainActivity.instance ?: return

        dialog(
            activity = activity,
            title = context.resources.getString(R.string.install_dialog),
            msg = context.resources.getString(R.string.install_dialog_desc),
            cancelable = false,
            okText = installLabel,
            cancelText = context.resources.getString(R.string.later),
            onOk = { onConfirm() },
            onCancel = {}
        )
    }

    private fun showUpdateDialog(onConfirm: () -> Unit) {
        val updateLabel = context.appResources.getString("update") ?: "Update"
        val activity = MainActivity.instance ?: return

        dialog(
            activity = activity,
            title = context.resources.getString(R.string.update_dialog),
            msg = context.resources.getString(R.string.update_dialog_desc),
            cancelable = false,
            okText = updateLabel,
            cancelText = context.resources.getString(R.string.later),
            onOk = { onConfirm() },
            onCancel = {}
        )
    }

    private fun showUninstallConfirmDialog(onConfirm: () -> Unit) {
        val uninstallLabel = context.appResources.getString("uninstall") ?: "Uninstall"
        val cancelLabel = context.appResources.getString("cancel") ?: "Cancel"
        val activity = MainActivity.instance ?: return

        dialog(
            activity = activity,
            title = context.resources.getString(R.string.uninstall_dialog),
            msg = context.resources.getString(R.string.uninstall_confirm_dialog_desc),
            cancelable = false,
            okText = uninstallLabel,
            cancelText = cancelLabel,
            onOk = { onConfirm() },
            onCancel = {}
        )
    }

    private fun showUninstallQuestionDialog(onConfirm: () -> Unit) {
        val uninstallLabel = context.appResources.getString("uninstall") ?: "Uninstall"
        val activity = context.currentActivity ?: return

        dialog(
            activity = activity,
            title = context.resources.getString(R.string.uninstall_dialog),
            msg = context.resources.getString(R.string.uninstall_question_dialog_desc),
            cancelable = false,
            okText = uninstallLabel,
            cancelText = context.resources.getString(R.string.keep),
            onOk = { onConfirm() },
            onCancel = {}
        )
    }
}