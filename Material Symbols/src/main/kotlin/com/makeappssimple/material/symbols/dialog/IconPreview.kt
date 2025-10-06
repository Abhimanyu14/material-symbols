package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.JBColor
import javax.swing.Icon
import javax.swing.JLabel

internal class IconPreview() : JLabel() {
    init {
        alignmentX = CENTER_ALIGNMENT
        horizontalAlignment = CENTER
        verticalAlignment = CENTER
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
