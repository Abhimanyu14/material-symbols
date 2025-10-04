package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import javax.swing.JComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.swing.Swing

private const val dialogTitle = "Material Symbols"

public class MaterialSymbolsDialog(
    private val project: Project,
) : DialogWrapper(project) {
    // region coroutine
    private val coroutineScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Swing,
    )
    // endregion

    // region data
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel = MaterialSymbolsDialogViewModel(
        coroutineScope = coroutineScope,
    )
    // endregion

    init {
        init()
        initDialogUI()
    }

    override fun createCenterPanel(): JComponent {
        return MaterialSymbolsDialogPanel(
            coroutineScope = coroutineScope,
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            closeDialog = {
                close(CLOSE_EXIT_CODE)
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
    }

    override fun doOKAction() {
        val androidDirectoryHelper = AndroidDirectoryHelper(
            project = project,
            showErrorDialog = {
                showErrorDialog(
                    errorMessage = it,
                )
            }
        )
        val drawableDirectory: PsiDirectory = androidDirectoryHelper.getDrawableDirectory() ?: return
        for (selectedMaterialSymbol in materialSymbolsDialogViewModel.selectedMaterialSymbols) {
            try {
                val fileName = materialSymbolsDialogViewModel.getFileName(
                    materialSymbol = selectedMaterialSymbol,
                )
                WriteCommandAction.runWriteCommandAction(project) {
                    val drawableFile: PsiFile = drawableDirectory.createFile(fileName)
                    androidDirectoryHelper.downloadDrawableFile(
                        drawableFile = drawableFile,
                        drawableResourceFileContent = materialSymbolsDialogViewModel.getDrawableResourceFileContent(
                            materialSymbol = selectedMaterialSymbol,
                        ),
                    )
                }
            } catch (
                exception: Exception,
            ) {
                showErrorDialog(
                    errorMessage = "Failed to download the icons: ${exception.message}",
                )
                close(CLOSE_EXIT_CODE)
            }
        }
        super.doOKAction()
    }

    override fun dispose() {
        super.dispose()
        coroutineScope.cancel()
    }

    private fun initDialogUI() {
        title = dialogTitle
        isOKActionEnabled = false
    }

    // region error dialog
    private fun showErrorDialog(
        errorMessage: String,
    ) {
        Messages.showErrorDialog(project, errorMessage, "Error")
    }
    // endregion
}
