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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class TypstInstallationAction {
    INSTALL,
    UPDATE,
    UNINSTALL,
}

data class TypstInstallationManager(
    private val script: File,
    private val context: ExtensionContext,
) {

    companion object {
        const val TYPST_PATH = "/home/.local/bin/typst"
    }

    var cachedPendingAction: TypstInstallationAction? = null
        private set

    fun performStartupActions() {
        context.scope.launch {
            val pendingAction = checkForAction()

            if (pendingAction == TypstInstallationAction.UPDATE) {
                withContext(Dispatchers.Main) {
                    showUpdateDialog { manageInstallation(pendingAction) }
                }
            }
        }
    }

    fun performAction(action: TypstInstallationAction) {
        when (action) {
            TypstInstallationAction.INSTALL ->
                showInstallDialog {
                    manageInstallation(action)
                }
            TypstInstallationAction.UPDATE ->
                showUpdateDialog {
                    manageInstallation(action)
                }
            TypstInstallationAction.UNINSTALL ->
                showUninstallConfirmDialog {
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

    private suspend fun checkForAction(): TypstInstallationAction? {
        val action =
            when {
                !isCliInstalled() -> TypstInstallationAction.INSTALL
                isUpdateAvailable() -> TypstInstallationAction.UPDATE
                else -> null
            }

        cachedPendingAction = action
        return action
    }

    fun isCliInstalled(): Boolean {
        return sandboxHomeDir().child(TYPST_PATH.removePrefix("/home/")).exists()
    }

    fun ensureCliInstalled(): Boolean {
        if (!isCliInstalled()) {
            toast(context.resources.getString(R.string.cli_not_installed))
            performAction(TypstInstallationAction.INSTALL)
            return false
        }
        return true
    }

    private suspend fun isUpdateAvailable(): Boolean {
        if (!isCliInstalled()) return false
        val currentVersion = getInstalledVersion()?.toVersionOrNull(false) ?: return false
        val latestVersion = fetchLatestVersion().toVersionOrNull(false) ?: return false

        return currentVersion < latestVersion
    }

    private suspend fun getInstalledVersion(): String? {
        val result =
            ShellUtils.runUbuntu(
                command = arrayOf(TYPST_PATH, "--version"),
                timeoutSeconds = 20L,
            )
        if (result.timedOut || result.exitCode != 0) return null

        return runCatching {
            Regex("""\d+\.\d+\.\d+""").find(result.output)?.value
        }
            .getOrNull()
    }

    private suspend fun fetchLatestVersion() = GithubReleasesApi("typst", "typst").fetchLatestVersion() ?: "v0.15.0"

    private fun manageInstallation(action: TypstInstallationAction) {
        context.scope.launch {
            val flags =
                when (action) {
                    TypstInstallationAction.INSTALL -> arrayOf("--install", fetchLatestVersion())
                    TypstInstallationAction.UPDATE -> arrayOf("--update")
                    TypstInstallationAction.UNINSTALL -> arrayOf("--uninstall")
                }

            val activity = MainActivity.instance ?: return@launch

            launchTerminal(
                activity = activity,
                terminalCommand =
                    TerminalCommand(
                        exe = "/bin/bash",
                        args =
                            arrayOf(
                                script.absolutePath,
                                *flags,
                            ),
                        id = "Typst installation",
                        env = arrayOf("DEBIAN_FRONTEND=noninteractive"),
                    ),
            )
        }
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
            onCancel = {},
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
            onCancel = {},
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
            onCancel = {},
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
            onCancel = {},
        )
    }
}
