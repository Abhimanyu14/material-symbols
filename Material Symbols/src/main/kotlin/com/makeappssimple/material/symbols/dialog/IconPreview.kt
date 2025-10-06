package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.JBColor
import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JLabel

internal class IconPreview() : JLabel() {
    init {
        alignmentX = CENTER_ALIGNMENT
        horizontalAlignment = CENTER
        verticalAlignment = CENTER
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(super.getMaximumSize().width, preferredSize.height)
    }

    fun updateIcon(
        updatedIcon: Icon,
    ) {
        icon = updatedIcon
        isOpaque = true
        background = JBColor.WHITE
        repaint()
    }
}
