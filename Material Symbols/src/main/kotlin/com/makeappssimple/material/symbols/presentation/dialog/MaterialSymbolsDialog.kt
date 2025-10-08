package com.makeappssimple.material.symbols.presentation.dialog

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.makeappssimple.material.symbols.presentation.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.presentation.resources.ResourcesProvider
import javax.swing.JComponent

public class MaterialSymbolsDialog(
    private val project: Project,
) : DialogWrapper(project) {
    private var materialSymbolsDialogPanel: MaterialSymbolsDialogPanel? = null
    private val resourcesProvider: ResourcesProvider = ResourcesProvider()

    init {
        init()
    }

    override fun createCenterPanel(): JComponent {
        initDialogUI()
        return createMaterialSymbolsDialogPanel()
    }

    override fun doOKAction() {
        materialSymbolsDialogPanel?.saveSelectedDrawableResources()
        super.doOKAction()
    }

    override fun dispose() {
        super.dispose()
        materialSymbolsDialogPanel?.dispose()
    }

    private fun initDialogUI() {
        title = resourcesProvider.dialogTitle
        isOKActionEnabled = false
    }

    private fun createMaterialSymbolsDialogPanel(): MaterialSymbolsDialogPanel {
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
            resourcesProvider = resourcesProvider,
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

    private fun showErrorDialog(
        errorMessage: String,
    ) {
        Messages.showErrorDialog(project, errorMessage, resourcesProvider.errorErrorTitle)
    }
}
