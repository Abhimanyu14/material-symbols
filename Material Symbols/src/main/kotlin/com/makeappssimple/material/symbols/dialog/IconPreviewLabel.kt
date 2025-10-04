package com.makeappssimple.material.symbols.dialog

import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JLabel

internal const val previewIconSize = 96

internal class IconPreviewLabel() : JLabel() {
    init {
        size = Dimension(previewIconSize, previewIconSize)
    }

    fun updateIcon(
        updatedIcon: Icon,
    ) {
        icon = updatedIcon
    }
}
