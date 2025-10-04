package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.ui.CheckBoxList
import com.intellij.ui.SearchTextField
import com.makeappssimple.material.symbols.android.AndroidDirectoryHelper
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing

private const val dialogTitle = "Material Symbols"
private const val minimumHeight = 600
private const val minimumWidth = 700
private const val previewIconSize = 96

public class MaterialSymbolsDialog(
    private val project: Project,
) : DialogWrapper(project) {
    // region coroutines
    private val coroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Swing,
    )
    // endregion

    // region data
    private val viewModel = MaterialSymbolsDialogViewModel(
        coroutineScope = coroutineScope,
    )
    private val remoteIconLoader = RemoteIconLoader()
    // endregion

    // region UI elements
    private val progressBar = JProgressBar()
    private val searchTextField = SearchTextField()
    private val listPanel = JPanel(BorderLayout())
    private val previewLabel: JLabel = JLabel()
    private var currentPreviewMaterialSymbol: String = "10k"
    private val materialSymbolCheckBoxList = CheckBoxList<MaterialSymbol>()
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
        for (selectedMaterialSymbol in viewModel.selectedMaterialSymbols) {
            try {
                val fileName = viewModel.getFileName(
                    materialSymbol = selectedMaterialSymbol,
                )
                WriteCommandAction.runWriteCommandAction(project) {
                    val drawableFile: PsiFile = drawableDirectory.createFile(fileName)
                    downloadDrawableFile(
                        drawableFile = drawableFile,
                        drawableResourceFileContent = viewModel.getDrawableResourceFileContent(
                            materialSymbol = selectedMaterialSymbol,
                        ),
                    )
                }
            } catch (
                exception: Exception,
            ) {
                hideProgressBar()
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
        viewModel.dispose()
        coroutineScope.cancel()
    }

    private fun createOptionsPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            // region filled
            val fillCheckBox = JCheckBox("Filled")
            fillCheckBox.isSelected = viewModel.selectedFill
            fillCheckBox.addActionListener { actionEvent ->
                viewModel.selectedFill = (actionEvent.source as JCheckBox).isSelected
                onOptionsUpdated()
            }
            add(fillCheckBox)
            // endregion

            // region style
            add(JLabel("Style:"))
            val styleComboBox = ComboBox(
                MaterialSymbolsStyle.values(),
            )
            styleComboBox.selectedItem = viewModel.selectedStyle
            styleComboBox.addItemListener { itemEvent ->
                if (itemEvent.stateChange == ItemEvent.SELECTED) {
                    viewModel.selectedStyle = itemEvent.item as MaterialSymbolsStyle
                    onOptionsUpdated()
                }
            }
            add(styleComboBox)
            // endregion

            // region weight
            add(JLabel("Weight:"))
            val weightComboBox = ComboBox(MaterialSymbolsWeight.values())
            weightComboBox.selectedItem = viewModel.selectedWeight
            weightComboBox.addItemListener { itemEvent ->
                if (itemEvent.stateChange == ItemEvent.SELECTED) {
                    viewModel.selectedWeight = itemEvent.item as MaterialSymbolsWeight
                    onOptionsUpdated()
                }
            }
            add(weightComboBox)
            // endregion

            // region grade
            add(JLabel("Grade:"))
            val gradeComboBox = ComboBox(MaterialSymbolsGrade.values())
            gradeComboBox.selectedItem = viewModel.selectedGrade
            gradeComboBox.addItemListener { itemEvent ->
                if (itemEvent.stateChange == ItemEvent.SELECTED) {
                    viewModel.selectedGrade = itemEvent.item as MaterialSymbolsGrade
                    onOptionsUpdated()
                }
            }
            add(gradeComboBox)
            // endregion

            // region size
            add(JLabel("Size:"))
            val sizeComboBox = ComboBox(MaterialSymbolsSize.values())
            sizeComboBox.selectedItem = viewModel.selectedSize
            sizeComboBox.addItemListener { itemEvent ->
                if (itemEvent.stateChange == ItemEvent.SELECTED) {
                    viewModel.selectedSize = itemEvent.item as MaterialSymbolsSize
                    onOptionsUpdated()
                }
            }
            add(sizeComboBox)
            // endregion
        }
    }

    private fun createPreviewPanel(): JLabel {
        return previewLabel.apply {
            icon = ScaledIcon(
                icon = RemoteUrlIcon(
                    coroutineScope = coroutineScope,
                    remoteIconLoader = remoteIconLoader,
                    iconUrl = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsrounded/${currentPreviewMaterialSymbol}/default/48px.svg",
                    onIconLoaded = {
                        repaint()
                    },
                ),
                width = previewIconSize,
                height = previewIconSize,
            )
            size = Dimension(previewIconSize, previewIconSize)
        }
    }

    private fun createContentPanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            add(searchTextField, BorderLayout.NORTH)
            add(listPanel, BorderLayout.CENTER)
        }
    }

    private fun initUI() {
        title = dialogTitle
        isOKActionEnabled = false
        progressBar.isIndeterminate = true

        materialSymbolCheckBoxList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        materialSymbolCheckBoxList.layoutOrientation = JList.HORIZONTAL_WRAP
        materialSymbolCheckBoxList.visibleRowCount = -1

        materialSymbolCheckBoxList.cellRenderer = MyCellRenderer(
            checkBoxList = materialSymbolCheckBoxList,
            coroutineScope = coroutineScope,
            materialSymbolsDialogViewModel = viewModel,
            remoteIconLoader = remoteIconLoader,
            onCellSelected = { selectedCellIndex ->
                previewLabel.apply {
                    val updatedSelectedMaterialSymbol = materialSymbolCheckBoxList.getItemAt(
                        selectedCellIndex
                    )?.name.orEmpty()
                    if (currentPreviewMaterialSymbol != updatedSelectedMaterialSymbol) {
                        currentPreviewMaterialSymbol = updatedSelectedMaterialSymbol
                        icon = ScaledIcon(
                            icon = RemoteUrlIcon(
                                coroutineScope = coroutineScope,
                                remoteIconLoader = remoteIconLoader,
                                iconUrl = "https://fonts.gstatic.com/s/i/short-term/release/materialsymbolsrounded/${
                                    currentPreviewMaterialSymbol
                                }/default/48px.svg",
                                onIconLoaded = {
                                    repaint()
                                },
                            ),
                            width = previewIconSize,
                            height = previewIconSize,
                        )
                    }
                }
            },
        )
        initListeners()
    }

    private fun initListeners() {
        materialSymbolCheckBoxList.setCheckBoxListListener { index, isChecked ->
            materialSymbolCheckBoxList.getItemAt(index)?.let {
                if (isChecked) {
                    viewModel.selectedMaterialSymbols.add(
                        element = it,
                    )
                } else {
                    viewModel.selectedMaterialSymbols.remove(
                        element = it,
                    )
                }
            }
            isOKActionEnabled = viewModel.selectedMaterialSymbols.isNotEmpty()
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

    private fun fetchData() {
        showProgressBar()
        coroutineScope.launch {
            try {
                viewModel.getAllIcons()
                viewModel.allMaterialSymbols.forEach { materialSymbol ->
                    materialSymbolCheckBoxList.addItem(materialSymbol, materialSymbol.title, false)
                }
                val scrollPane = JScrollPane(materialSymbolCheckBoxList).apply {
                    border = BorderFactory.createEmptyBorder()
                }
                addToListPanelCenter(
                    component = scrollPane,
                )
                hideProgressBar()
                listPanel.revalidate()
                listPanel.repaint()
            } catch (
                exception: Exception,
            ) {
                hideProgressBar()
                showErrorDialog(
                    message = "Failed to load icons: ${exception.message}",
                )
                close(CLOSE_EXIT_CODE)
            }
        }
    }

    private fun filterMaterialSymbols() {
        val selectedMaterialSymbolsSet = viewModel.selectedMaterialSymbols.toSet()
        val filteredMaterialSymbols = if (searchTextField.text.isBlank()) {
            viewModel.allMaterialSymbols
        } else {
            viewModel.allMaterialSymbols.filter { materialSymbol ->
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

    private fun addToListPanelCenter(
        component: Component,
    ) {
        listPanel.add(component, BorderLayout.CENTER)
    }

    private fun onOptionsUpdated() {
        materialSymbolCheckBoxList.repaint()
    }

    // region error dialog
    private fun showErrorDialog(
        message: String,
    ) {
        Messages.showErrorDialog(project, message, "Error")
    }
    // endregion

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
