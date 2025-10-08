package com.makeappssimple.material.symbols.presentation.dialog

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.view.ViewBox
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon

internal class ScaledIcon(
    val svgDocument: SVGDocument,
    val size: Int,
) : Icon {
    override fun paintIcon(
        c: Component?,
        g: Graphics?,
        x: Int,
        y: Int,
    ) {
        val graphics2D = g as? Graphics2D ?: return
        graphics2D.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        svgDocument.render(
            c,
            graphics2D,
            ViewBox(
                x.toFloat(),
                y.toFloat(),
                size.toFloat(),
                size.toFloat(),
            ),
        )
    }

    override fun getIconWidth(): Int {
        return size
    }

    override fun getIconHeight(): Int {
        return size
    }
}
