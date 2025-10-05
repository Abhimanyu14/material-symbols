package com.makeappssimple.material.symbols.dialog

import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JLabel

internal const val previewLabelSize = 96

internal class IconPreviewLabel() : JLabel() {
    init {
        minimumSize = Dimension(previewLabelSize, previewLabelSize)
        size = Dimension(previewLabelSize, previewLabelSize)
        preferredSize = Dimension(previewLabelSize, previewLabelSize)
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
