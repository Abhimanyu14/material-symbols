package com.makeappssimple.material.symbols.repository

import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolOptions

internal interface MaterialSymbolsRepository {
    suspend fun getAllIcons(): List<MaterialSymbol>

    suspend fun getIconUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String

    fun getDrawableResourceFileInfo(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): DrawableResourceFileInfo
}
