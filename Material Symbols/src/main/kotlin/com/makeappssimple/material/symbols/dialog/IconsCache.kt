package com.makeappssimple.material.symbols.dialog

import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

internal class IconsCache {
    private val iconsCache = ConcurrentHashMap<String, Icon>()

    fun getIcon(
        iconUrl: String,
    ): Icon? {
        return iconsCache[iconUrl]
    }

    fun cacheIcon(
        iconUrl: String,
        icon: Icon,
    ) {
        iconsCache[iconUrl] = icon
    }
}
