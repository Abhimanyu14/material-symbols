package com.makeappssimple.material.symbols.dialog

import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JLabel

internal const val previewIconSize = 96

internal class IconPreviewLabel() : JLabel() {
    init {
        minimumSize = Dimension(previewIconSize, previewIconSize)
        preferredSize = Dimension(previewIconSize, previewIconSize)
        size = Dimension(previewIconSize, previewIconSize)
        alignmentX = CENTER_ALIGNMENT
        horizontalAlignment = CENTER
        verticalAlignment = CENTER
    }

    fun updateIcon(
        updatedIcon: Icon,
    ) {
        icon = updatedIcon
    }
}
