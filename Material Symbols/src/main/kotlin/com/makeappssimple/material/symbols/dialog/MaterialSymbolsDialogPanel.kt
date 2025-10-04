package com.makeappssimple.material.symbols.dialog

import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val minimumHeight = 600
private const val minimumWidth = 700

internal class MaterialSymbolsDialogPanel(
    private val coroutineScope: CoroutineScope,
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val closeDialog: () -> Unit,
    private val showErrorDialog: (errorMessage: String) -> Unit,
    private val updateOkButtonEnabled: (Boolean) -> Unit,
) : JPanel() {
    // region data
    private val iconsCache: IconsCache = IconsCache()
    private var currentPreviewMaterialSymbol: String = "10k"
    // endregion

    // region UI elements
    private lateinit var iconPreviewLabel: IconPreviewLabel
    private lateinit var contentPanel: ContentPanel
    // endregion

    init {
        initUI()
        fetchData()
    }

    private fun initUI() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        minimumSize = Dimension(minimumWidth, minimumHeight)

        add(createOptionsPanel())
        add(createPreviewPanel())
        add(createContentPanel())
    }

    private fun fetchData() {
        contentPanel.loadAllIcons(
            onError = { exception ->
                showErrorDialog("Failed to load icons: ${exception.message}")
                closeDialog()
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
        iconPreviewLabel = IconPreviewLabel()
        updatePreviewIcon()
        return iconPreviewLabel
    }

    private fun createContentPanel(): JPanel {
        contentPanel = ContentPanel(
            coroutineScope = coroutineScope,
            iconsCache = iconsCache,
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            updateOkButtonEnabled = {
                updateOkButtonEnabled(materialSymbolsDialogViewModel.selectedMaterialSymbols.isNotEmpty())
            },
            onPreviewMaterialSymbolUpdated = { updatedSelectedMaterialSymbol ->
                if (currentPreviewMaterialSymbol != updatedSelectedMaterialSymbol) {
                    currentPreviewMaterialSymbol = updatedSelectedMaterialSymbol
                    updatePreviewIcon()
                }
            }
        )
        return contentPanel
    }

    private fun updatePreviewIcon() {
        coroutineScope.launch {
            val icon = iconsCache.getIcon(
                iconUrl = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsrounded/${currentPreviewMaterialSymbol}/default/48px.svg",
            )
            icon?.let {
                iconPreviewLabel.updateIcon(
                    updatedIcon = RemoteUrlIcon(
                        icon = icon,
                    ),
                )
                iconPreviewLabel.repaint()
            }
        }
    }
}
