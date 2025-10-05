package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.ui.ComboBox
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

internal class OptionsPanel(
    initialFilledValue: Boolean,
    initialGrade: MaterialSymbolsGrade,
    initialSize: MaterialSymbolsSize,
    initialStyle: MaterialSymbolsStyle,
    initialWeight: MaterialSymbolsWeight,
    resourcesProvider: ResourcesProvider,
    onFilledValueChange: (Boolean) -> Unit,
    onGradeChange: (MaterialSymbolsGrade) -> Unit,
    onSizeChange: (MaterialSymbolsSize) -> Unit,
    onStyleChange: (MaterialSymbolsStyle) -> Unit,
    onWeightChange: (MaterialSymbolsWeight) -> Unit,
) : JPanel() {
    private val isFilledCheckBox = JCheckBox(resourcesProvider.filledLabel)
    private val gradeComboBox = ComboBox(MaterialSymbolsGrade.values())
    private val styleComboBox = ComboBox(MaterialSymbolsStyle.values())
    private val weightComboBox = ComboBox(MaterialSymbolsWeight.values())
    private val sizeComboBox = ComboBox(MaterialSymbolsSize.values())

    init {
        layout = FlowLayout(FlowLayout.LEFT)

        // region filled
        isFilledCheckBox.isSelected = initialFilledValue
        isFilledCheckBox.addActionListener { actionEvent ->
            onFilledValueChange((actionEvent.source as JCheckBox).isSelected)
        }
        add(isFilledCheckBox)
        // endregion

        // region style
        add(JLabel(resourcesProvider.styleLabel))

        styleComboBox.selectedItem = initialStyle
        styleComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onStyleChange(itemEvent.item as MaterialSymbolsStyle)
            }
        }
        add(styleComboBox)
        // endregion

        // region size
        add(JLabel(resourcesProvider.sizeLabel))

        sizeComboBox.selectedItem = initialSize
        sizeComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onSizeChange(itemEvent.item as MaterialSymbolsSize)
            }
        }
        add(sizeComboBox)
        // endregion

        // region weight
        add(JLabel(resourcesProvider.weightLabel))

        weightComboBox.selectedItem = initialWeight
        weightComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onWeightChange(itemEvent.item as MaterialSymbolsWeight)
            }
        }
        add(weightComboBox)
        // endregion

        // region grade
        add(JLabel(resourcesProvider.gradeLabel))

        gradeComboBox.selectedItem = initialGrade
        gradeComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onGradeChange(itemEvent.item as MaterialSymbolsGrade)
            }
        }
        add(gradeComboBox)
        // endregion
    }
}
