package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.intellij.ui.SearchTextField
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class ContentPanel(
    private val coroutineScope: CoroutineScope,
    private val iconsCache: IconsCache,
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val resourcesProvider: ResourcesProvider,
    private val updateOkButtonEnabled: () -> Unit,
    private val onPreviewMaterialSymbolUpdated: (String) -> Unit,
) : JPanel() {
    private val progressBar = JProgressBar()
    private val searchTextField = SearchTextField()
    private val listPanel = JPanel(BorderLayout())
    private val materialSymbolCheckBoxList = CheckBoxList<MaterialSymbol>()

    init {
        layout = BorderLayout()

        progressBar.isIndeterminate = true
        materialSymbolCheckBoxList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        materialSymbolCheckBoxList.layoutOrientation = JList.HORIZONTAL_WRAP
        materialSymbolCheckBoxList.visibleRowCount = -1

        materialSymbolCheckBoxList.cellRenderer = MyCellRenderer(
            checkBoxList = materialSymbolCheckBoxList,
            coroutineScope = coroutineScope,
            iconsCache = iconsCache,
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            resourcesProvider = resourcesProvider,
            onCellSelected = { selectedCellIndex ->
                val updatedSelectedMaterialSymbol: String = materialSymbolCheckBoxList.getItemAt(
                    selectedCellIndex
                )?.name.orEmpty()
                onPreviewMaterialSymbolUpdated(updatedSelectedMaterialSymbol)
            },
        )
        initListeners()

        add(searchTextField, BorderLayout.NORTH)
        add(listPanel, BorderLayout.CENTER)
    }

    fun initListeners() {
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
        searchTextField.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(
                    e: DocumentEvent?,
                ) {
                    filterMaterialSymbols()
                }

                override fun removeUpdate(
                    e: DocumentEvent?,
                ) {
                    filterMaterialSymbols()
                }

                override fun changedUpdate(
                    e: DocumentEvent?,
                ) {
                    filterMaterialSymbols()
                }
            },
        )
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
                materialSymbolsDialogViewModel.fetchAllIcons()
                materialSymbolsDialogViewModel.filteredMaterialSymbols.forEach { materialSymbol ->
                    materialSymbolCheckBoxList.addItem(materialSymbol, materialSymbol.title, false)
                }
                val scrollPane = JScrollPane(materialSymbolCheckBoxList).apply {
                    border = BorderFactory.createEmptyBorder()
                }
                addToListPanelCenter(
                    component = scrollPane,
                )
                hideProgressBar()
                refreshListPanel()
            } catch (
                exception: Exception,
            ) {
                hideProgressBar()
                onError(exception)
            }
        }
    }

    private fun filterMaterialSymbols() {
        val selectedMaterialSymbolsSet = materialSymbolsDialogViewModel.selectedMaterialSymbols.toSet()
        materialSymbolsDialogViewModel.updateFilteredMaterialSymbols(
            searchText = searchTextField.text,
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

    private fun addToListPanelCenter(
        component: Component,
    ) {
        listPanel.add(component, BorderLayout.CENTER)
    }

    private fun refreshListPanel() {
        listPanel.revalidate()
        listPanel.repaint()
    }

    // region progressbar
    private fun showProgressBar() {
        addToListPanelCenter(
            component = progressBar,
        )
    }

    private fun hideProgressBar() {
        listPanel.remove(progressBar)
    }
    // endregion
}
