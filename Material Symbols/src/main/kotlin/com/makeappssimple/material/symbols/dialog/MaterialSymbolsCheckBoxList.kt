package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.makeappssimple.material.symbols.cache.IconsCache
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.BorderLayout
import java.util.concurrent.ConcurrentHashMap
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal class MaterialSymbolsCheckBoxList(
    private val coroutineScope: CoroutineScope,
    private val iconsCache: IconsCache,
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val resourcesProvider: ResourcesProvider,
    private val updateOkButtonEnabled: () -> Unit,
    private val onPreviewMaterialSymbolUpdated: (MaterialSymbol) -> Unit,
) : JPanel() {
    private val progressBar = JProgressBar()
    private val listPanel = JPanel(BorderLayout())
    private val materialSymbolCheckBoxList = CheckBoxList<MaterialSymbol>()
    private val iconsMap: ConcurrentHashMap<MaterialSymbol, Icon> = ConcurrentHashMap()

    init {
        layout = BorderLayout()

        progressBar.isIndeterminate = true
        progressBar.isStringPainted = true
        progressBar.string = resourcesProvider.dialogProgress

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
        add(listPanel, BorderLayout.CENTER)

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
                val allIcons: List<MaterialSymbol> = materialSymbolsDialogViewModel.getAllIcons()
                allIcons.forEach { materialSymbol ->
                    materialSymbolCheckBoxList.addItem(materialSymbol, materialSymbol.title, false)
                }
                hideProgressBar()
                val scrollPane = JScrollPane(materialSymbolCheckBoxList).apply {
                    border = BorderFactory.createEmptyBorder()
                }
                listPanel.add(scrollPane, BorderLayout.CENTER)
                initIconsMap(
                    allIcons = allIcons,
                )
                refreshListPanel()
            } catch (
                exception: Exception,
            ) {
                hideProgressBar()
                onError(exception)
            }
        }
    }

    private suspend fun initIconsMap(
        allIcons: List<MaterialSymbol>,
    ) {
        coroutineScope {
            allIcons.forEach { materialSymbol ->
                launch {
                    val icon = iconsCache.getIcon(
                        iconUrl = materialSymbolsDialogViewModel.getIconUrl(
                            materialSymbol = materialSymbol,
                        ),
                    )
                    val scaledIcon: Icon? = icon?.let {
                        ScaledIcon(
                            icon = icon,
                            size = 12,
                        )
                    }
                    scaledIcon?.let {
                        iconsMap[materialSymbol] = scaledIcon
                    }
                }
            }
        }
    }

    fun filterMaterialSymbols(
        searchText: String,
    ) {
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
        listPanel.revalidate()
        listPanel.repaint()
    }

    // region progressbar
    private fun showProgressBar() {
        listPanel.add(progressBar, BorderLayout.NORTH)
    }

    private fun hideProgressBar() {
        listPanel.remove(progressBar)
    }
    // endregion
}
