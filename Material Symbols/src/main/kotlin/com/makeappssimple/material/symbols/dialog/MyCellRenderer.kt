package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.CheckBoxList
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.viewmodel.MaterialSymbolsDialogViewModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants
import kotlinx.coroutines.CoroutineScope

internal class MyCellRenderer(
    private val checkBoxList: CheckBoxList<MaterialSymbol>,
    private val coroutineScope: CoroutineScope,
    private val materialSymbolsDialogViewModel: MaterialSymbolsDialogViewModel,
    private val remoteIconLoader: RemoteIconLoader,
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
            return JLabel(
                "Error",
            )
        }

        // Use the provided JCheckBox as the root component.
        // JCheckBox is a container, so we can add components to it.
        value.layout = BorderLayout()
        value.border = BorderFactory.createEmptyBorder(0, 0, 0, 16)

        // Clear previous components to avoid duplication on cell reuse
        value.removeAll()

        // Add the icon to the checkbox component
        value.add(iconLabel, BorderLayout.WEST)

        val materialSymbol = (list as CheckBoxList<MaterialSymbol>).getItemAt(index)
        if (materialSymbol != null) {
            iconLabel.icon = RemoteUrlIcon(
                coroutineScope = coroutineScope,
                remoteIconLoader = remoteIconLoader,
                iconUrl = materialSymbolsDialogViewModel.getIconUrl(
                    materialSymbol = materialSymbol,
                ),
                onIconLoaded = {
                    checkBoxList.repaint(
                        checkBoxList.getCellBounds(
                            index,
                            index,
                        ),
                    )
                },
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
