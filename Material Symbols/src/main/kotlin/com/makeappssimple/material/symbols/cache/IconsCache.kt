package com.makeappssimple.material.symbols.cache

import com.intellij.openapi.util.IconLoader
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class IconsCache() {
    private val iconsCache = ConcurrentHashMap<String, Icon>()

    suspend fun getIcon(
        iconUrl: String,
    ): Icon? {
        return iconsCache[iconUrl] ?: fetchAndCacheIcon(
            iconUrl = iconUrl,
        )
    }

    private suspend fun fetchAndCacheIcon(
        iconUrl: String,
    ): Icon? {
        return withContext(
            context = Dispatchers.IO,
        ) {
            try {
                val loadedIcon: Icon? = IconLoader.findIcon(
                    url = URI.create(iconUrl).toURL(),
                    storeToCache = true,
                )
                loadedIcon?.also {
                    cacheIcon(
                        iconUrl = iconUrl,
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
        icon: Icon,
    ) {
        iconsCache[iconUrl] = icon
    }
}
