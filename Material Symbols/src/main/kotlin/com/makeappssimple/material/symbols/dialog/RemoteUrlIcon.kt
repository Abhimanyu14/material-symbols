package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.CheckBoxList
import com.makeappssimple.material.symbols.model.MaterialSymbol
import java.awt.Component
import java.awt.Graphics
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

private const val defaultIconSize = 60

internal class RemoteUrlIcon(
    private val coroutineScope: CoroutineScope,
    private val checkBoxList: CheckBoxList<MaterialSymbol>,
    private val cellIndex: Int,
    private val remoteIconLoader: RemoteIconLoader,
    private val iconUrl: String,
) : Icon {
    companion object {
        private val loadingUrls = ConcurrentHashMap.newKeySet<String>()
        private val waitingCells = ConcurrentHashMap<String, MutableSet<Pair<CheckBoxList<MaterialSymbol>, Int>>>()
    }

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
            waitingCells.computeIfAbsent(
                iconUrl,
            ) {
                ConcurrentHashMap.newKeySet()
            }.add(
                element = checkBoxList to cellIndex,
            )
            coroutineScope.launch(
                context = Dispatchers.IO,
            ) {
                loadIcon()
            }
        }
    }

    private suspend fun loadIcon() {
        if (!loadingUrls.add(iconUrl)) {
            return // Already loading
        }

        try {
            val loadedIcon = IconLoader.findIcon(
                iconUrl,
                RemoteUrlIcon::class.java,
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
