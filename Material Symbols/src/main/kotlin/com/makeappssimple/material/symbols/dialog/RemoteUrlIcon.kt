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
    // TODO(Abhi): Check if this can be removed
    /*
    companion object {
        private val loadingUrls = ConcurrentHashMap.newKeySet<String>()
        private val waitingCells = ConcurrentHashMap<String, MutableSet<Pair<CheckBoxList<MaterialSymbol>, Int>>>()
    }
    */

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
            // TODO(Abhi): Check if this can be removed
            /*
            waitingCells.computeIfAbsent(
                iconUrl,
            ) {
                ConcurrentHashMap.newKeySet()
            }.add(
                element = checkBoxList to cellIndex,
            )
            */
            coroutineScope.launch(
                context = Dispatchers.IO,
            ) {
                loadIcon()
                onIconLoaded()
            }
        }
    }

    private fun loadIcon() {
        // TODO(Abhi): Check if this can be removed
        /*
        if (!loadingUrls.add(iconUrl)) {
            return // Already loading
        }
        */
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
            exception: Exception,
        ) {
        } finally {
            // TODO(Abhi): Check if this can be removed
            /*
            loadingUrls.remove(
                iconUrl,
            )
            waitingCells.remove(
                iconUrl,
            )?.forEach { (targetList, targetIndex) ->
                withContext(
                    context = Dispatchers.Swing,
                ) {
                    targetList.repaint(
                        targetList.getCellBounds(
                            targetIndex,
                            targetIndex,
                        ),
                    )
                }
            }
            */
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
