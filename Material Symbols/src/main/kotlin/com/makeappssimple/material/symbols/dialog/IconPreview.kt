package com.makeappssimple.material.symbols.dialog

import javax.swing.Icon
import javax.swing.JLabel

internal const val previewLabelSize = 96

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
        repaint()
    }
}
