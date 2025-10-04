package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.swing.Swing

private const val dialogTitle = "Material Symbols"
private const val minimumHeight = 600
private const val minimumWidth = 700

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
    private val iconsCache: IconsCache = IconsCache()
    private var currentPreviewMaterialSymbol: String = "10k"
    // endregion

    // region UI elements
    private lateinit var iconPreviewLabel: IconPreviewLabel
    private lateinit var contentPanel: ContentPanel
    // endregion

    init {
        init()
        initUI()
        fetchData()
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            minimumSize = Dimension(minimumWidth, minimumHeight)

            add(createOptionsPanel())
            add(createPreviewPanel())
            add(createContentPanel())
        }
    }

    override fun doOKAction() {
        val drawableDirectory: PsiDirectory = getDrawableDirectory() ?: return
        for (selectedMaterialSymbol in materialSymbolsDialogViewModel.selectedMaterialSymbols) {
            try {
                val fileName = materialSymbolsDialogViewModel.getFileName(
                    materialSymbol = selectedMaterialSymbol,
                )
                WriteCommandAction.runWriteCommandAction(project) {
                    val drawableFile: PsiFile = drawableDirectory.createFile(fileName)
                    downloadDrawableFile(
                        drawableFile = drawableFile,
                        drawableResourceFileContent = materialSymbolsDialogViewModel.getDrawableResourceFileContent(
                            materialSymbol = selectedMaterialSymbol,
                        ),
                    )
                }
            } catch (
                exception: Exception,
            ) {
                contentPanel.hideProgressBar()
                showErrorDialog(
                    message = "Failed to download the icons: ${exception.message}",
                )
                close(CLOSE_EXIT_CODE)
            }
        }
        super.doOKAction()
    }

    private fun getDrawableDirectory(): PsiDirectory? {
        val androidDirectoryHelper = AndroidDirectoryHelper(
            project = project,
        )
        if (!androidDirectoryHelper.isAndroidPluginInstalled()) {
            showErrorDialog(
                message = "Android support plugin is not enabled!",
            )
            return null
        }
        return try {
            val drawableDirectory = androidDirectoryHelper.getDrawableDirectory()
            if (drawableDirectory == null) {
                showErrorDialog(
                    message = "Could not find or create a drawable directory. Is this an Android project?",
                )
                null
            } else {
                drawableDirectory
            }
        } catch (
            _: NoClassDefFoundError,
        ) {
            // This is a safeguard, the check above should prevent this.
            showErrorDialog(
                message = "Android support is not available.",
            )
            null
        }
    }

    override fun dispose() {
        super.dispose()
        materialSymbolsDialogViewModel.dispose()
        coroutineScope.cancel()
    }

    private fun initUI() {
        title = dialogTitle
        isOKActionEnabled = false
    }

    private fun fetchData() {
        contentPanel.loadAllIcons(
            onError = { exception ->
                showErrorDialog(
                    message = "Failed to load icons: ${exception.message}",
                )
                close(CLOSE_EXIT_CODE)
            },
        )
    }

    private fun createOptionsPanel(): JPanel {
        return OptionsPanel(
            initialFilledValue = materialSymbolsDialogViewModel.isFilled,
            initialGrade = materialSymbolsDialogViewModel.selectedGrade,
            initialSize = materialSymbolsDialogViewModel.selectedSize,
            initialStyle = materialSymbolsDialogViewModel.selectedStyle,
            initialWeight = materialSymbolsDialogViewModel.selectedWeight,
            onFilledValueChange = {
                materialSymbolsDialogViewModel.isFilled = it
                contentPanel.repaintMaterialSymbolCheckBoxList()
            },
            onGradeChange = {
                materialSymbolsDialogViewModel.selectedGrade = it
                contentPanel.repaintMaterialSymbolCheckBoxList()
            },
            onSizeChange = {
                materialSymbolsDialogViewModel.selectedSize = it
                contentPanel.repaintMaterialSymbolCheckBoxList()
            },
            onStyleChange = {
                materialSymbolsDialogViewModel.selectedStyle = it
                contentPanel.repaintMaterialSymbolCheckBoxList()
            },
            onWeightChange = {
                materialSymbolsDialogViewModel.selectedWeight = it
                contentPanel.repaintMaterialSymbolCheckBoxList()
            },
        )
    }

    private fun createPreviewPanel(): JLabel {
        iconPreviewLabel = IconPreviewLabel().apply {
            updateIcon(
                updatedIcon = RemoteUrlIcon(
                    coroutineScope = coroutineScope,
                    iconsCache = iconsCache,
                    iconUrl = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsrounded/${currentPreviewMaterialSymbol}/default/48px.svg",
                    width = previewIconSize,
                    height = previewIconSize,
                    onIconLoaded = {
                        repaint()
                    },
                ),
            )
        }
        return iconPreviewLabel
    }

    private fun createContentPanel(): JPanel {
        contentPanel = ContentPanel(
            coroutineScope = coroutineScope,
            iconsCache = iconsCache,
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            updateOkButtonEnabled = {
                isOKActionEnabled = materialSymbolsDialogViewModel.selectedMaterialSymbols.isNotEmpty()
            },
            onPreviewMaterialSymbolUpdated = { updatedSelectedMaterialSymbol ->
                if (currentPreviewMaterialSymbol != updatedSelectedMaterialSymbol) {
                    currentPreviewMaterialSymbol = updatedSelectedMaterialSymbol
                    iconPreviewLabel.updateIcon(
                        updatedIcon = RemoteUrlIcon(
                            coroutineScope = coroutineScope,
                            iconsCache = iconsCache,
                            iconUrl = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsrounded/${
                                currentPreviewMaterialSymbol
                            }/default/48px.svg",
                            width = previewIconSize,
                            height = previewIconSize,
                            onIconLoaded = {
                                repaint()
                            },
                        ),
                    )
                }
            }
        )
        return contentPanel
    }

    private fun downloadDrawableFile(
        drawableFile: PsiFile,
        drawableResourceFileContent: String,
    ) {
        try {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                val document = psiDocumentManager.getDocument(drawableFile)
                if (document != null) {
                    document.setText(drawableResourceFileContent)
                    psiDocumentManager.commitDocument(document)
                }
                drawableFile.virtualFile?.let {
                    FileEditorManager.getInstance(project).openFile(it, true)
                }
            }
        } catch (
            exception: Exception,
        ) {
            showErrorDialog(
                message = "Error downloading or saving file: ${exception.message}",
            )
        }
    }

    // region error dialog
    private fun showErrorDialog(
        message: String,
    ) {
        Messages.showErrorDialog(project, message, "Error")
    }
    // endregion
}
