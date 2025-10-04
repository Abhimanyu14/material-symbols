package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import javax.swing.JComponent

private const val dialogTitle = "Material Symbols"

public class MaterialSymbolsDialog(
    private val project: Project,
) : DialogWrapper(project) {
    private var materialSymbolsDialogPanel: MaterialSymbolsDialogPanel? = null

    init {
        init()
        initDialogUI()
    }

    override fun createCenterPanel(): JComponent {
        val androidDirectoryHelper = AndroidDirectoryHelper(
            project = project,
            showErrorDialog = {
                showErrorDialog(
                    errorMessage = it,
                )
            }
        )
        val dialogPanel = MaterialSymbolsDialogPanel(
            androidDirectoryHelper = androidDirectoryHelper,
            closeDialog = {
                close(CLOSE_EXIT_CODE)
            },
            runWriteCommandAction = { commandAction ->
                WriteCommandAction.runWriteCommandAction(project) {
                    commandAction()
                }
            },
            showErrorDialog = { errorMessage ->
                showErrorDialog(
                    errorMessage = errorMessage,
                )
            },
            updateOkButtonEnabled = {
                isOKActionEnabled = it
            },
        )
        materialSymbolsDialogPanel = dialogPanel
        return dialogPanel
    }

    override fun doOKAction() {
        super.doOKAction()
        materialSymbolsDialogPanel?.saveSelectedDrawableResources()
    }

    override fun dispose() {
        super.dispose()
        materialSymbolsDialogPanel?.dispose()
    }

    private fun initDialogUI() {
        title = dialogTitle
        isOKActionEnabled = false
    }

    private fun showErrorDialog(
        errorMessage: String,
    ) {
        Messages.showErrorDialog(project, errorMessage, "Error")
    }
}
