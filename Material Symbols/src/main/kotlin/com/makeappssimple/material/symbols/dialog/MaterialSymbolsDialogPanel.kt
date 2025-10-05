package com.makeappssimple.material.symbols.dialog

import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing

private const val minimumHeight = 600
private const val minimumWidth = 700

internal class MaterialSymbolsDialogPanel(
    private val androidDirectoryHelper: AndroidDirectoryHelper,
    private val resourcesProvider: ResourcesProvider,
    private val closeDialog: () -> Unit,
    private val runWriteCommandAction: (commandAction: () -> Unit) -> Unit,
    private val showErrorDialog: (errorMessage: String) -> Unit,
    private val updateOkButtonEnabled: (Boolean) -> Unit,
) : JPanel() {
    // region coroutine
    private val coroutineScope: CoroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Swing,
    )
    // endregion

    // region data
    private val iconsCache: IconsCache = IconsCache()
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel = MaterialSymbolsDialogViewModel(
        coroutineScope = coroutineScope,
    )
    private var currentPreviewMaterialSymbol: MaterialSymbol? = null
    // endregion

    // region UI elements
    private lateinit var iconPreviewLabel: IconPreviewLabel
    private lateinit var contentPanel: ContentPanel
    // endregion

    init {
        initUI()
        fetchData()
    }

    fun saveSelectedDrawableResources() {
        materialSymbolsDialogViewModel.getSelectedMaterialSymbolsDrawableResourceFileInfoList()
            .forEach { drawableResourceFileInfo ->
                try {
                    runWriteCommandAction {
                        androidDirectoryHelper.saveDrawableFile(
                            drawableResourceFileInfo = drawableResourceFileInfo,
                        )
                    }
                } catch (
                    exception: Exception,
                ) {
                    showErrorDialog("${resourcesProvider.downloadErrorPrefix} ${exception.message}")
                    closeDialog()
                }
            }
    }

    fun dispose() {
        coroutineScope.cancel()
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
                showErrorDialog("${resourcesProvider.loadErrorPrefix} ${exception.message}")
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
            resourcesProvider = resourcesProvider,
            onFilledValueChange = {
                materialSymbolsDialogViewModel.isFilled = it
                onOptionsUpdated()
            },
            onGradeChange = {
                materialSymbolsDialogViewModel.selectedGrade = it
                onOptionsUpdated()
            },
            onSizeChange = {
                materialSymbolsDialogViewModel.selectedSize = it
                onOptionsUpdated()
            },
            onStyleChange = {
                materialSymbolsDialogViewModel.selectedStyle = it
                onOptionsUpdated()
            },
            onWeightChange = {
                materialSymbolsDialogViewModel.selectedWeight = it
                onOptionsUpdated()
            },
        )
    }

    private fun onOptionsUpdated() {
        updatePreviewIcon()
        contentPanel.repaintMaterialSymbolCheckBoxList()
    }

    private fun createPreviewPanel(): JLabel {
        iconPreviewLabel = IconPreviewLabel()
        currentPreviewMaterialSymbol = materialSymbolsDialogViewModel.filteredMaterialSymbols.firstOrNull()
        updatePreviewIcon()
        return iconPreviewLabel
    }

    private fun createContentPanel(): JPanel {
        contentPanel = ContentPanel(
            coroutineScope = coroutineScope,
            iconsCache = iconsCache,
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            resourcesProvider = resourcesProvider,
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
        val materialSymbol = currentPreviewMaterialSymbol ?: return
        coroutineScope.launch {
            val iconUrl = materialSymbolsDialogViewModel.getIconUrl(
                materialSymbol = materialSymbol,
            )
            val icon = iconsCache.getIcon(
                iconUrl = iconUrl,
            )
            icon?.let {
                iconPreviewLabel.updateIcon(
                    updatedIcon = RemoteUrlIcon(
                        icon = icon,
                        size = icon.iconHeight * 4,
                    ),
                )
                iconPreviewLabel.repaint()
            }
        }
    }
}
