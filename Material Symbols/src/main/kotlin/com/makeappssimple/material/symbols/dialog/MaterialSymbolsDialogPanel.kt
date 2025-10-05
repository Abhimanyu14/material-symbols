package com.makeappssimple.material.symbols.dialog

import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.android.facet.AndroidFacet

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
    private var currentModule: AndroidFacet? = null
    // endregion

    // region UI elements
    private lateinit var iconPreview: IconPreview
    private lateinit var searchBar: SearchBar
    private lateinit var materialSymbolsCheckBoxList: MaterialSymbolsCheckBoxList
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
                        currentModule?.let {
                            androidDirectoryHelper.saveDrawableFile(
                                drawableResourceFileInfo = drawableResourceFileInfo,
                                selectedModule = it,
                            )
                        }
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
        add(createModulesPanel())
        add(createIconPreview())
        add(createSearchBar())
        add(createMaterialSymbolsCheckBoxList())
    }

    private fun fetchData() {
        materialSymbolsCheckBoxList.loadAllIcons(
            onError = { exception ->
                showErrorDialog("${resourcesProvider.loadErrorPrefix} ${exception.message}")
                closeDialog()
            },
        )
    }

    private fun createOptionsPanel(): OptionsPanel {
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

    private fun createModulesPanel(): ModulesPanel {
        val androidFacets: Array<AndroidFacet> = androidDirectoryHelper.getAndroidFacets().toTypedArray()
        currentModule = androidFacets.firstOrNull()
        return ModulesPanel(
            androidFacets = androidFacets,
            initialModule = currentModule,
            resourcesProvider = resourcesProvider,
            onModuleChange = {
                currentModule = it
            },
        )
    }

    private fun onOptionsUpdated() {
        updatePreviewIcon()
        materialSymbolsCheckBoxList.repaintMaterialSymbolCheckBoxList()
    }

    private fun createIconPreview(): IconPreview {
        iconPreview = IconPreview()
        currentPreviewMaterialSymbol = materialSymbolsDialogViewModel.filteredMaterialSymbols.firstOrNull()
        updatePreviewIcon()
        return iconPreview
    }

    private fun createSearchBar(): SearchBar {
        searchBar = SearchBar(
            onSearchTextUpdate = { searchText ->
                materialSymbolsCheckBoxList.filterMaterialSymbols(
                    searchText = searchText,
                )
            },
        )
        return searchBar
    }

    private fun createMaterialSymbolsCheckBoxList(): MaterialSymbolsCheckBoxList {
        materialSymbolsCheckBoxList = MaterialSymbolsCheckBoxList(
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
        return materialSymbolsCheckBoxList
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
                iconPreview.updateIcon(
                    updatedIcon = RemoteUrlIcon(
                        icon = icon,
                        size = previewLabelSize,
                    ),
                )
                iconPreview.repaint()
            }
        }
    }
}
