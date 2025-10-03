package com.makeappssimple.material.symbols.dialog

import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

internal class RemoteIconLoader {
    private val iconCache = ConcurrentHashMap<String, Icon>()

    fun getIcon(
        iconUrl: String,
    ): Icon? {
        return iconCache[iconUrl]
    }

    fun cacheIcon(
        iconUrl: String,
        icon: Icon,
    ) {
        iconCache[iconUrl] = icon
    }
}
