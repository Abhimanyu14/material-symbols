package com.makeappssimple.material.symbols.data.network

import java.io.File

internal interface IconDataSource {
    suspend fun getAllIcons(
        cacheFile: File? = null,
    ): List<String>
}
