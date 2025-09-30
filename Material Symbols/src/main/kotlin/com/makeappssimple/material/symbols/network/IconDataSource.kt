package com.makeappssimple.material.symbols.network

import java.io.File

internal interface IconDataSource {
    suspend fun getAllIcons(
        cacheFile: File? = null,
    ): List<String>
}
