package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

internal const val cellHeight = 32

internal class MyCellRenderer(
    private val iconsMap: Map<MaterialSymbol, Icon>,
    private val resourcesProvider: ResourcesProvider,
    private val onCellSelected: (Int) -> Unit,
) : ListCellRenderer<JCheckBox> {
    private val iconLabel = JLabel()

    init {
        iconLabel.verticalAlignment = SwingConstants.CENTER
        iconLabel.horizontalAlignment = SwingConstants.CENTER
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
            return JLabel(resourcesProvider.cellError)
        }

        // Clear previous components to avoid duplication on cell reuse
        value.removeAll()

        // Use the provided JCheckBox as the root component.
        // JCheckBox is a container, so we can add components to it.
        value.isOpaque = true
        value.iconTextGap = 36

        value.border = BorderFactory.createEmptyBorder(0, 0, 0, 16)
        iconLabel.border = BorderFactory.createEmptyBorder(0, 24, 0, 4)

        // Add the icon to the checkbox component
        value.add(iconLabel)

        val materialSymbol = (list as CheckBoxList<MaterialSymbol>).getItemAt(index)
        if (materialSymbol != null) {
            iconLabel.icon = iconsMap[materialSymbol]
        }

        // Apply selection colors
        if (isSelected) {
            onCellSelected(index)
            value.background = list.selectionBackground
        } else {
            value.background = list.background
        }
        iconLabel.background = value.background
        return value
    }
}
