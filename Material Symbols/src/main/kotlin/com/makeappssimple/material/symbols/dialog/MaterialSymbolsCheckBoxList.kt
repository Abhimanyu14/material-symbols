package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.makeappssimple.material.symbols.cache.SvgDocumentCache
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import io.ktor.utils.io.CancellationException
import java.util.concurrent.ConcurrentHashMap
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing

internal class MaterialSymbolsCheckBoxList(
    private val coroutineScope: CoroutineScope,
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val resourcesProvider: ResourcesProvider,
    private val svgDocumentCache: SvgDocumentCache,
    private val updateOkButtonEnabled: () -> Unit,
    private val onPreviewMaterialSymbolUpdated: (MaterialSymbol) -> Unit,
) : JPanel() {
    private val allIcons: MutableList<MaterialSymbol> = mutableListOf()
    private val progressBar = JProgressBar()
    private val materialSymbolCheckBoxList = CheckBoxList<MaterialSymbol>()
    private val iconsMap: ConcurrentHashMap<MaterialSymbol, Icon> = ConcurrentHashMap()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        materialSymbolCheckBoxList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        materialSymbolCheckBoxList.layoutOrientation = JList.HORIZONTAL_WRAP
        materialSymbolCheckBoxList.visibleRowCount = -1

        materialSymbolCheckBoxList.cellRenderer = MyCellRenderer(
            iconsMap = iconsMap,
            resourcesProvider = resourcesProvider,
            onCellSelected = { selectedCellIndex ->
                val updatedSelectedMaterialSymbol: MaterialSymbol? = materialSymbolCheckBoxList.getItemAt(
                    selectedCellIndex
                )
                updatedSelectedMaterialSymbol?.let {
                    onPreviewMaterialSymbolUpdated(updatedSelectedMaterialSymbol)
                }
            },
        )
        materialSymbolCheckBoxList.fixedCellHeight = cellHeight

        initListeners()
    }

    fun repaintMaterialSymbolCheckBoxList() {
        materialSymbolCheckBoxList.repaint()
    }

    fun loadAllIcons(
        onError: (exception: Exception) -> Unit,
    ) {
        showProgressBar()
        coroutineScope.launch {
            try {
                allIcons.addAll(
                    elements = materialSymbolsDialogViewModel.getAllIcons(),
                )
                launch(
                    context = coroutineContext + Dispatchers.Swing,
                ) {
                    allIcons.forEach { materialSymbol ->
                        materialSymbolCheckBoxList.addItem(
                            materialSymbol,
                            materialSymbol.title,
                            false,
                        )
                    }
                    hideProgressBar()
                    val scrollPane = JScrollPane(materialSymbolCheckBoxList).apply {
                        border = BorderFactory.createEmptyBorder()
                    }
                    add(scrollPane)
                    refreshListPanel()
                }
                refreshIconsMap()
            } catch (
                cancellationException: CancellationException,
            ) {
                throw cancellationException
            } catch (
                exception: Exception,
            ) {
                hideProgressBar()
                onError(exception)
            }
        }
    }

    suspend fun refreshIconsMap() {
        coroutineScope {
            allIcons.forEach { materialSymbol ->
                launch {
                    val iconUrl = materialSymbolsDialogViewModel.getIconUrl(
                        materialSymbol = materialSymbol,
                    )
                    val svgDocument = svgDocumentCache.getSvgDocument(
                        iconUrl = iconUrl,
                    )
                    svgDocument?.let {
                        iconsMap[materialSymbol] = ScaledIcon(
                            svgDocument = svgDocument,
                            size = cellIconSize,
                        )
                        launch(
                            context = coroutineContext + Dispatchers.Swing,
                        ) {
                            refreshListPanel()
                        }
                    }
                }
            }
        }
    }

    fun filterMaterialSymbols(
        searchText: String,
    ) {
        coroutineScope.launch {
            val selectedMaterialSymbolsSet = materialSymbolsDialogViewModel.selectedMaterialSymbols.toSet()
            materialSymbolsDialogViewModel.updateFilteredMaterialSymbols(
                searchText = searchText,
            )
            materialSymbolCheckBoxList.clear()
            materialSymbolsDialogViewModel.filteredMaterialSymbols.forEach { filteredMaterialSymbol ->
                materialSymbolCheckBoxList.addItem(
                    filteredMaterialSymbol,
                    filteredMaterialSymbol.title,
                    selectedMaterialSymbolsSet.contains(
                        element = filteredMaterialSymbol,
                    ),
                )
            }
        }
    }

    private fun initListeners() {
        materialSymbolCheckBoxList.setCheckBoxListListener { index, isChecked ->
            materialSymbolCheckBoxList.getItemAt(index)?.let { materialSymbol ->
                if (isChecked) {
                    materialSymbolsDialogViewModel.addToSelectedMaterialSymbols(
                        materialSymbol = materialSymbol,
                    )
                } else {
                    materialSymbolsDialogViewModel.removeFromSelectedMaterialSymbols(
                        materialSymbol = materialSymbol,
                    )
                }
            }
            updateOkButtonEnabled()
        }
    }

    private fun refreshListPanel() {
        revalidate()
        repaint()
    }

    // region progressbar
    private fun showProgressBar() {
        progressBar.isIndeterminate = true
        progressBar.isStringPainted = true
        progressBar.string = resourcesProvider.dialogProgress
        add(progressBar)
    }

    private fun hideProgressBar() {
        remove(progressBar)
    }
    // endregion
}
