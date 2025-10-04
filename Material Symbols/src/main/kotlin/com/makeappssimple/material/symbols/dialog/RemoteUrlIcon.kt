package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.util.IconLoader
import java.awt.Component
import java.awt.Graphics
import java.net.URI
import javax.swing.Icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val defaultIconSize = 60

internal class RemoteUrlIcon(
    private val coroutineScope: CoroutineScope,
    private val remoteIconLoader: RemoteIconLoader,
    private val iconUrl: String,
    private val onIconLoaded: () -> Unit,
) : Icon {
    override fun paintIcon(
        c: Component,
        g: Graphics,
        x: Int,
        y: Int,
    ) {
        val icon = remoteIconLoader.getIcon(
            iconUrl = iconUrl,
        )
        if (icon != null) {
            icon.paintIcon(c, g, x, y)
        } else {
            coroutineScope.launch(
                context = Dispatchers.IO,
            ) {
                loadIcon()
                onIconLoaded()
            }
        }
    }

    private fun loadIcon() {
        try {
            val loadedIcon = IconLoader.findIcon(
                url = URI.create(iconUrl).toURL(),
                storeToCache = true,
            )
            loadedIcon?.let {
                remoteIconLoader.cacheIcon(
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
        val icon = remoteIconLoader.getIcon(
            iconUrl = iconUrl,
        )
        return icon?.iconWidth ?: defaultIconSize
    }

    override fun getIconHeight(): Int {
        val icon = remoteIconLoader.getIcon(
            iconUrl = iconUrl,
        )
        return icon?.iconHeight ?: defaultIconSize
    }
}
