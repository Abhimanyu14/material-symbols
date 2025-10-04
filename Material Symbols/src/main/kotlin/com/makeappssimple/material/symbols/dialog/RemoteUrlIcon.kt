package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.util.IconLoader
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.net.URI
import javax.swing.Icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val defaultIconSize = 60

internal class RemoteUrlIcon(
    private val coroutineScope: CoroutineScope,
    private val iconsCache: IconsCache,
    private val iconUrl: String,
    private val width: Int = defaultIconSize,
    private val height: Int = defaultIconSize,
    private val onIconLoaded: () -> Unit,
) : Icon {
    override fun paintIcon(
        c: Component?,
        g: Graphics?,
        x: Int,
        y: Int,
    ) {
        val icon: Icon? = iconsCache.getIcon(
            iconUrl = iconUrl,
        )
        if (icon != null) {
            paintScaledIcon(
                icon = icon,
                c = c,
                g = g,
                x = x,
                y = y,
            )
        } else {
            coroutineScope.launch(
                context = Dispatchers.IO,
            ) {
                loadIcon()
                onIconLoaded()
            }
        }
    }

    private fun paintScaledIcon(
        icon: Icon,
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

    private fun loadIcon() {
        try {
            val loadedIcon = IconLoader.findIcon(
                url = URI.create(iconUrl).toURL(),
                storeToCache = true,
            )
            loadedIcon?.let {
                iconsCache.cacheIcon(
                    iconUrl = iconUrl,
                    icon = loadedIcon,
                )
            }
        } catch (
            _: Exception,
        ) {
        } finally {
        }
    }

    override fun getIconWidth(): Int {
        return width
    }

    override fun getIconHeight(): Int {
        return height
    }
}
