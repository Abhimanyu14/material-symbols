package com.makeappssimple.material.symbols.dialog

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.Icon

internal class ScaledIcon(
    private val icon: Icon,
    private val width: Int,
    private val height: Int,
) : Icon {
    override fun paintIcon(
        c: Component?,
        g: Graphics?,
        x: Int,
        y: Int,
    ) {
        val graphics2D = g?.create() as? Graphics2D
        graphics2D?.translate(x, y)
        val scaleX = width.toDouble() / icon.iconWidth
        val scaleY = height.toDouble() / icon.iconHeight
        graphics2D?.scale(scaleX, scaleY)
        icon.paintIcon(c, graphics2D, 0, 0)
        graphics2D?.dispose()
    }

    override fun getIconWidth(): Int {
        return width
    }

    override fun getIconHeight(): Int {
        return height
    }
}
