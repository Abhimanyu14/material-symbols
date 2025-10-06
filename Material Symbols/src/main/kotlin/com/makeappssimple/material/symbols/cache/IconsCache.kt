package com.makeappssimple.material.symbols.cache

import com.intellij.ui.svg.loadSvg
import java.awt.image.BufferedImage
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class IconsCache() {
    private val iconsCache = ConcurrentHashMap<Pair<String, Int>, Icon>()

    suspend fun getScaledIcon(
        iconUrl: String,
        scale: Int = 1,
    ): Icon? {
        return iconsCache[Pair(iconUrl, scale)] ?: fetchAndCacheIcon(
            iconUrl = iconUrl,
            scale = scale,
        )
    }

    private suspend fun fetchAndCacheIcon(
        iconUrl: String,
        scale: Int,
    ): Icon? {
        return withContext(
            context = Dispatchers.IO,
        ) {
            try {
                val url = URI.create(iconUrl).toURL()
                val svgContent = url.readText()
                val bufferedImage: BufferedImage = loadSvg(
                    data = svgContent.byteInputStream().readBytes(),
                    scale = scale.toFloat(),
                )
                val loadedIcon = ImageIcon(bufferedImage)
                loadedIcon.also {
                    cacheIcon(
                        iconUrl = iconUrl,
                        scale = scale,
                        icon = loadedIcon,
                    )
                }
            } catch (
                cancellationException: CancellationException,
            ) {
                throw cancellationException
            } catch (
                _: Exception,
            ) {
                null
            }
        }
    }

    private fun cacheIcon(
        iconUrl: String,
        scale: Int,
        icon: Icon,
    ) {
        iconsCache[Pair(iconUrl, scale)] = icon
    }
}
