package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.intellij.ui.SearchTextField
import com.makeappssimple.material.symbols.model.MaterialSymbol
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
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val remoteIconLoader: RemoteIconLoader,
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
            materialSymbolsDialogViewModel = materialSymbolsDialogViewModel,
            remoteIconLoader = remoteIconLoader,
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
            materialSymbolCheckBoxList.getItemAt(index)?.let {
                if (isChecked) {
                    materialSymbolsDialogViewModel.selectedMaterialSymbols.add(
                        element = it,
                    )
                } else {
                    materialSymbolsDialogViewModel.selectedMaterialSymbols.remove(
                        element = it,
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

    private fun filterMaterialSymbols() {
        val selectedMaterialSymbolsSet = materialSymbolsDialogViewModel.selectedMaterialSymbols.toSet()
        val filteredMaterialSymbols = if (searchTextField.text.isBlank()) {
            materialSymbolsDialogViewModel.allMaterialSymbols
        } else {
            materialSymbolsDialogViewModel.allMaterialSymbols.filter { materialSymbol ->
                materialSymbol.title.contains(
                    other = searchTextField.text,
                    ignoreCase = true,
                )
            }
        }
        materialSymbolCheckBoxList.clear()
        filteredMaterialSymbols.forEach { filteredMaterialSymbol ->
            materialSymbolCheckBoxList.addItem(
                filteredMaterialSymbol,
                filteredMaterialSymbol.title,
                selectedMaterialSymbolsSet.contains(
                    element = filteredMaterialSymbol,
                ),
            )
        }
    }

    fun addToListPanelCenter(
        component: Component,
    ) {
        listPanel.add(component, BorderLayout.CENTER)
    }

    fun repaintMaterialSymbolCheckBoxList() {
        materialSymbolCheckBoxList.repaint()
    }

    // region progressbar
    fun showProgressBar() {
        addToListPanelCenter(
            component = progressBar,
        )
    }

    fun hideProgressBar() {
        listPanel.remove(progressBar)
    }
    // endregion

    fun refreshListPanel() {
        listPanel.revalidate()
        listPanel.repaint()
    }

    fun loadAllIcons(
        onError: (exception: Exception) -> Unit,
    ) {
        showProgressBar()
        coroutineScope.launch {
            try {
                materialSymbolsDialogViewModel.getAllIcons()
                materialSymbolsDialogViewModel.allMaterialSymbols.forEach { materialSymbol ->
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
}
