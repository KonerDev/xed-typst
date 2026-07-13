package com.koner.typst.commands.compile

import com.koner.typst.utils.TypstInstallationManager
import com.rk.exec.ShellUtils
import com.rk.extension.ExtensionContext
import com.rk.file.FileObject
import com.rk.file.FileOperations
import com.rk.file.toFileWrapper
import com.rk.utils.toast
import java.io.File

internal suspend fun compile(context: ExtensionContext, fileObject: FileObject, targetType: String = "pdf") {
    val workingDir = fileObject.getParentFile()?.getAbsolutePath()
    val result =
        ShellUtils.runUbuntu(
            workingDir,
            TypstInstallationManager.TYPST_PATH,
            "compile",
            "--format",
            targetType,
            fileObject.getName(),
            timeoutSeconds = 5,
        )

    if (result.timedOut || result.exitCode != 0) {
        context.logError("Compile error: \n${result.error}")
        toast("Compile error: ${result.error}")
    } else {
        context.logInfo("Compile success: \n${result.output}")

        val compiledName = fileObject.getName().removePrefix(fileObject.getExtension()) + targetType
        FileOperations.openWithExternalApp(
            context.appContext,
            File(compiledName).toFileWrapper(),
        )
    }
}
