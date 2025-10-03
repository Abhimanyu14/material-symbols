package com.makeappssimple.material.symbols.dialog

import com.android.tools.idea.projectsystem.SourceProviderManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.CheckBoxList
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.CheckBox
import com.intellij.ui.components.Label
import com.intellij.ui.components.Panel
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
import java.awt.Graphics
import java.awt.event.ItemEvent
import java.util.concurrent.ConcurrentHashMap
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import org.jetbrains.android.facet.AndroidFacet

private const val dialogTitle = "Material Symbols"

public class MaterialSymbolsDialog(
    private val project: Project,
) : DialogWrapper(project) {
    // region coroutines
    private val coroutineScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Swing,
    )
    // endregion

    // region data
    private val viewModel = MaterialSymbolsDialogViewModel()
    private val iconCache = ConcurrentHashMap<String, Icon>()
    // endregion

    // region UI elements
    private val progressBar = JProgressBar()
    private val searchTextField = SearchTextField()
    private val listPanel = Panel(
        layout = BorderLayout(),
    )
    private val materialSymbolCheckBoxList = CheckBoxList<MaterialSymbol>()
    // endregion

    init {
        init()
        initUI()
        fetchData()
    }

    override fun createCenterPanel(): JComponent {
        return Panel(
            layout = BorderLayout(),
        ).apply {
            minimumSize = Dimension(
                700,
                600,
            )
            add(
                createOptionsPanel(),
                BorderLayout.NORTH,
            )
            add(
                createContentPanel(),
                BorderLayout.CENTER,
            )
        }
    }

    override fun doOKAction() {
        val drawableDirectory: PsiDirectory = getDrawableDirectory() ?: return
        for (selectedMaterialSymbol in viewModel.selectedMaterialSymbols) {
            try {
                val fileName = viewModel.getFileName(
                    materialSymbol = selectedMaterialSymbol,
                )
                WriteCommandAction.runWriteCommandAction(
                    project,
                ) {
                    val drawableFile: PsiFile = drawableDirectory.createFile(
                        fileName,
                    )
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
                close(
                    CLOSE_EXIT_CODE,
                )
            }
        }
        super.doOKAction()
    }

    private fun getDrawableDirectory(): PsiDirectory? {
        val androidFacet = project.modules.firstNotNullOfOrNull {
            AndroidFacet.getInstance(
                it,
            )
        }
        if (androidFacet == null) {
            showErrorDialog(
                message = "Android facet not found!",
            )
            return null
        }

        val sourceProvidersManager = SourceProviderManager.getInstance(
            androidFacet,
        )
        val resourceDirectoryFile = sourceProvidersManager.sources.resDirectories.firstOrNull()
        if (resourceDirectoryFile == null) {
            showErrorDialog(
                message = "Resource directory not found.",
            )
            return null
        }

        val psiManager = PsiManager.getInstance(
            project,
        )
        val resourceDirectory = psiManager.findDirectory(
            resourceDirectoryFile,
        )
        if (resourceDirectory == null) {
            showErrorDialog(
                message = "Could not find resource directory.",
            )
            return null
        }

        var drawableDirectory: PsiDirectory? = null
        WriteCommandAction.runWriteCommandAction(
            project,
        ) {
            drawableDirectory = resourceDirectory.findSubdirectory(
                "drawable",
            ) ?: resourceDirectory.createSubdirectory(
                "drawable",
            )
        }
        if (drawableDirectory == null) {
            showErrorDialog(
                message = "Could not find or create drawable directory.",
            )
            return null
        }
        return drawableDirectory
    }

    override fun dispose() {
        super.dispose()
        viewModel.dispose()
        coroutineScope.cancel()
    }

    private fun createOptionsPanel(): JPanel {
        return JPanel(
            FlowLayout(
                FlowLayout.LEFT,
            ),
        ).apply {
            // region filled
            val fillCheckBox = CheckBox(
                text = "Filled",
            )
            fillCheckBox.isSelected = viewModel.selectedFill
            fillCheckBox.addActionListener { actionEvent ->
                viewModel.selectedFill = (actionEvent.source as JCheckBox).isSelected
                onOptionsUpdated()
            }
            add(fillCheckBox)
            // endregion

            // region style
            add(
                Label(
                    text = "Style:",
                ),
            )
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
            add(
                Label(
                    text = "Weight:",
                ),
            )
            val weightComboBox = ComboBox(
                MaterialSymbolsWeight.values(),
            )
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
            add(
                Label(
                    text = "Grade:",
                ),
            )
            val gradeComboBox = ComboBox(
                MaterialSymbolsGrade.values(),
            )
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
            add(
                Label(
                    text = "Size:",
                ),
            )
            val sizeComboBox = ComboBox(
                MaterialSymbolsSize.values(),
            )
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

    private fun createContentPanel(): JPanel {
        return Panel(
            layout = BorderLayout(),
        ).apply {
            add(
                searchTextField,
                BorderLayout.NORTH,
            )
            add(
                listPanel,
                BorderLayout.CENTER,
            )
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
            list = materialSymbolCheckBoxList,
            viewModel = viewModel,
            iconCache = iconCache,
            coroutineScope = coroutineScope,
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
                    materialSymbolCheckBoxList.addItem(
                        materialSymbol,
                        materialSymbol.title,
                        false,
                    )
                }
                val scrollPane = JScrollPane(
                    materialSymbolCheckBoxList,
                ).apply {
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
                close(
                    CLOSE_EXIT_CODE,
                )
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
            WriteCommandAction.runWriteCommandAction(
                project,
            ) {
                val psiDocumentManager = PsiDocumentManager.getInstance(
                    project,
                )
                val document = psiDocumentManager.getDocument(
                    drawableFile,
                )
                if (document != null) {
                    document.setText(
                        drawableResourceFileContent,
                    )
                    psiDocumentManager.commitDocument(
                        document,
                    )
                }
                drawableFile.virtualFile?.let {
                    FileEditorManager.getInstance(
                        project,
                    ).openFile(
                        it,
                        true,
                    )
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
        listPanel.add(
            component,
            BorderLayout.CENTER,
        )
    }

    private fun onOptionsUpdated() {
        materialSymbolCheckBoxList.repaint()
    }

    // region error dialog
    private fun showErrorDialog(
        message: String,
    ) {
        Messages.showErrorDialog(
            project,
            message,
            "Error",
        )
    }
    // endregion

    // region progressbar
    private fun showProgressBar() {
        addToListPanelCenter(
            component = progressBar,
        )
    }

    private fun hideProgressBar() {
        listPanel.remove(
            progressBar,
        )
    }
    // endregion

    private class MyCellRenderer(
        private val list: CheckBoxList<MaterialSymbol>,
        private val viewModel: MaterialSymbolsDialogViewModel,
        private val iconCache: ConcurrentHashMap<String, Icon>,
        private val coroutineScope: CoroutineScope,
    ) : ListCellRenderer<JCheckBox> {
        private val iconLabel = JLabel()
        private val textLabel = JLabel()

        init {
            iconLabel.verticalAlignment = SwingConstants.CENTER
            iconLabel.horizontalAlignment = SwingConstants.CENTER
            val iconSize = 60
            val iconDimension = Dimension(iconSize, iconSize)
            iconLabel.preferredSize = iconDimension

            textLabel.verticalAlignment = SwingConstants.CENTER
            textLabel.horizontalAlignment = SwingConstants.LEFT
            textLabel.border = BorderFactory.createEmptyBorder(4, 16, 4, 16)
        }

        override fun getListCellRendererComponent(
            list: JList<out JCheckBox?>,
            value: JCheckBox?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            if (value == null) {
                // Should not happen with CheckBoxList, but good practice
                return JLabel("Error")
            }

            // Use the provided JCheckBox as the root component.
            // JCheckBox is a container, so we can add components to it.
            value.layout = BorderLayout()
            value.border = BorderFactory.createEmptyBorder(0, 0, 0, 16)

            // Clear previous components to avoid duplication on cell reuse
            value.removeAll()

            // Add the icon and text labels to the checkbox component
            value.add(iconLabel, BorderLayout.WEST)
            // value.add(textLabel, BorderLayout.CENTER)

            val materialSymbol = (list as CheckBoxList<MaterialSymbol>).getItemAt(index)
            if (materialSymbol != null) {
                iconLabel.icon = RemoteUrlIcon(
                    iconUrl = viewModel.getIconUrl(
                        materialSymbol = materialSymbol,
                    ),
                    iconCache = iconCache,
                    coroutineScope = coroutineScope,
                    list = this.list,
                    cellIndex = index,
                )
                textLabel.text = "<html>${materialSymbol.title}</html>"
            }

            // Apply selection colors
            if (isSelected) {
                value.background = list.selectionBackground
                textLabel.foreground = list.selectionForeground
            } else {
                value.background = list.background
                textLabel.foreground = list.foreground
            }
            iconLabel.background = value.background
            textLabel.background = value.background

            value.isOpaque = true
            value.iconTextGap = 28
            return value
        }
    }

    private class RemoteUrlIcon(
        private val iconUrl: String,
        private val iconCache: ConcurrentHashMap<String, Icon>,
        private val coroutineScope: CoroutineScope,
        private val list: CheckBoxList<MaterialSymbol>,
        private val cellIndex: Int,
    ) : Icon {
        companion object {
            private val loadingUrls = ConcurrentHashMap.newKeySet<String>()
            private val waitingCells = ConcurrentHashMap<String, MutableSet<Pair<CheckBoxList<MaterialSymbol>, Int>>>()
        }

        override fun paintIcon(
            c: Component,
            g: Graphics,
            x: Int,
            y: Int,
        ) {
            val icon = iconCache[iconUrl]
            if (icon != null) {
                icon.paintIcon(c, g, x, y)
            } else {
                waitingCells.computeIfAbsent(
                    iconUrl,
                ) {
                    ConcurrentHashMap.newKeySet()
                }.add(list to cellIndex)
                coroutineScope.launch {
                    loadIcon()
                }
            }
        }

        private suspend fun loadIcon() {
            if (!loadingUrls.add(iconUrl)) {
                return // Already loading
            }

            try {
                val loadedIcon = withContext(
                    context = Dispatchers.IO,
                ) {
                    IconLoader.findIcon(
                        iconUrl,
                        RemoteUrlIcon::class.java,
                    )
                }
                loadedIcon?.let {
                    iconCache[iconUrl] = loadedIcon
                }
            } catch (
                exception: Exception,
            ) {
            } finally {
                loadingUrls.remove(
                    iconUrl,
                )
                withContext(
                    context = Dispatchers.Swing,
                ) {
                    waitingCells.remove(
                        iconUrl,
                    )?.forEach { (targetList, targetIndex) ->
                        targetList.repaint(
                            targetList.getCellBounds(
                                targetIndex,
                                targetIndex,
                            ),
                        )
                    }
                }
            }
        }

        override fun getIconWidth(): Int {
            return iconCache[iconUrl]?.iconWidth ?: 60
        }

        override fun getIconHeight(): Int {
            return iconCache[iconUrl]?.iconHeight ?: 60
        }
    }
}
