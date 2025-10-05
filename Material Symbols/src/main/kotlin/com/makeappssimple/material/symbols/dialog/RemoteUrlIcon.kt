package com.makeappssimple.material.symbols.dialog

import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.Icon

internal class RemoteUrlIcon(
    private val icon: Icon,
    private val size: Int,
) : Icon {
    override fun paintIcon(
        c: Component?,
        g: Graphics?,
        x: Int,
        y: Int,
    ) {
        val bufferedImage: BufferedImage = UIUtil.createImage(
            c,
            icon.iconWidth,
            icon.iconHeight,
            BufferedImage.TYPE_INT_ARGB,
        )
        val imageGraphics = bufferedImage.createGraphics()
        icon.paintIcon(null, imageGraphics, 0, 0)
        imageGraphics?.dispose()

        g?.drawImage(bufferedImage, x, y, size, size, c)
    }

    override fun getIconWidth(): Int {
        return size
    }

    override fun getIconHeight(): Int {
        return size
    }
}
