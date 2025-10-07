package com.makeappssimple.material.symbols.dialog

import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.cache.SvgDocumentCache
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import io.ktor.utils.io.CancellationException
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
        context = SupervisorJob() + Dispatchers.IO,
    )
    // endregion

    // region data
    private var currentModule: AndroidFacet? = null
    private var currentPreviewMaterialSymbol: MaterialSymbol? = null
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel = MaterialSymbolsDialogViewModel(
    )
    private val svgDocumentCache: SvgDocumentCache = SvgDocumentCache()
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
        currentModule?.let { selectedModule ->
            val drawableResourceFileInfoList =
                materialSymbolsDialogViewModel.getSelectedMaterialSymbolsDrawableResourceFileInfoList()
            try {
                runWriteCommandAction {
                    androidDirectoryHelper.saveDrawableFiles(
                        drawableResourceFileInfoList = drawableResourceFileInfoList,
                        selectedModule = selectedModule,
                    )
                }
            } catch (
                cancellationException: CancellationException,
            ) {
                throw cancellationException
            } catch (
                _: Exception,
            ) {
                showErrorDialog(resourcesProvider.downloadErrorPrefix)
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
            onError = { _ ->
                showErrorDialog(resourcesProvider.loadErrorPrefix)
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
            onFilledValueChange = { updatedIsFilled ->
                materialSymbolsDialogViewModel.updateIsFilled(
                    updatedIsFilled = updatedIsFilled,
                )
                onOptionsUpdated()
            },
            onGradeChange = { updatedSelectedGrade ->
                materialSymbolsDialogViewModel.updateSelectedGrade(
                    updatedSelectedGrade = updatedSelectedGrade,
                )
                onOptionsUpdated()
            },
            onSizeChange = { updatedSelectedSize ->
                materialSymbolsDialogViewModel.updateSelectedSize(
                    updatedSelectedSize = updatedSelectedSize,
                )
                onOptionsUpdated()
            },
            onStyleChange = { updatedSelectedStyle ->
                materialSymbolsDialogViewModel.updateSelectedStyle(
                    updatedSelectedStyle = updatedSelectedStyle,
                )
                onOptionsUpdated()
            },
            onWeightChange = { updatedSelectedWeight ->
                materialSymbolsDialogViewModel.updateSelectedWeight(
                    updatedSelectedWeight = updatedSelectedWeight,
                )
                onOptionsUpdated()
            },
        )
    }

    private fun createModulesPanel(): ModulesPanel {
        val androidFacets: Array<AndroidFacet> = androidDirectoryHelper.getAndroidFacets().toTypedArray()
        currentModule = androidFacets.firstOrNull()
        return ModulesPanel(
            androidModules = androidFacets,
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
            svgDocumentCache = svgDocumentCache,
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
            val svgDocument = svgDocumentCache.getSvgDocument(
                iconUrl = iconUrl,
            )
            svgDocument?.let {
                iconPreview.updateIcon(
                    updatedIcon = ScaledIcon(
                        svgDocument = svgDocument,
                        size = iconPreviewSize,
                    ),
                )
            }
        }
    }
}
