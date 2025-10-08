package com.makeappssimple.material.symbols.presentation.dialog

import com.intellij.openapi.ui.ComboBox
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolOptions
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.presentation.resources.ResourcesProvider
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

internal class OptionsPanel(
    private val materialSymbolOptions: MaterialSymbolOptions,
    private val resourcesProvider: ResourcesProvider,
    private val onFilledValueChange: (Boolean) -> Unit,
    private val onGradeChange: (MaterialSymbolsGrade) -> Unit,
    private val onSizeChange: (MaterialSymbolsSize) -> Unit,
    private val onStyleChange: (MaterialSymbolsStyle) -> Unit,
    private val onWeightChange: (MaterialSymbolsWeight) -> Unit,
) : JPanel() {
    init {
        layout = FlowLayout(FlowLayout.LEFT)

        initFilledCheckBoxUI()
        initStyleComboBoxUI()
        initSizeComboBoxUI()
        initWeightComboBoxUI()
        initGradeComboBoxUI()
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(
            super.getMaximumSize().width,
            preferredSize.height,
        )
    }

    private fun initFilledCheckBoxUI() {
        val isFilledCheckBox = JCheckBox(resourcesProvider.filledLabel)
        isFilledCheckBox.isSelected = materialSymbolOptions.isFilled
        isFilledCheckBox.addActionListener { actionEvent ->
            onFilledValueChange((actionEvent.source as JCheckBox).isSelected)
        }
        add(isFilledCheckBox)
    }

    private fun initStyleComboBoxUI() {
        val styleComboBox = ComboBox(MaterialSymbolsStyle.values())
        styleComboBox.selectedItem = materialSymbolOptions.style
        styleComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onStyleChange(itemEvent.item as MaterialSymbolsStyle)
            }
        }
        add(JLabel(resourcesProvider.styleLabel))
        add(styleComboBox)
    }

    private fun initSizeComboBoxUI() {
        val sizeComboBox = ComboBox(MaterialSymbolsSize.values())
        sizeComboBox.selectedItem = materialSymbolOptions.size
        sizeComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onSizeChange(itemEvent.item as MaterialSymbolsSize)
            }
        }
        add(JLabel(resourcesProvider.sizeLabel))
        add(sizeComboBox)
    }

    private fun initWeightComboBoxUI() {
        val weightComboBox = ComboBox(MaterialSymbolsWeight.values())
        weightComboBox.selectedItem = materialSymbolOptions.weight
        weightComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onWeightChange(itemEvent.item as MaterialSymbolsWeight)
            }
        }
        add(JLabel(resourcesProvider.weightLabel))
        add(weightComboBox)
    }

    private fun initGradeComboBoxUI() {
        val gradeComboBox = ComboBox(MaterialSymbolsGrade.values())
        gradeComboBox.selectedItem = materialSymbolOptions.grade
        gradeComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onGradeChange(itemEvent.item as MaterialSymbolsGrade)
            }
        }
        add(JLabel(resourcesProvider.gradeLabel))
        add(gradeComboBox)
    }
}
