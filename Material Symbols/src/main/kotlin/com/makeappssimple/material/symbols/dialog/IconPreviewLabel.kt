package com.makeappssimple.material.symbols.dialog

import javax.swing.Icon
import javax.swing.JLabel

internal const val previewIconSize = 96

internal class IconPreviewLabel() : JLabel() {
    init {
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
