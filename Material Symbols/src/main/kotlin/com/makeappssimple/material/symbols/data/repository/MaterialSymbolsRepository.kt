package com.makeappssimple.material.symbols.data.repository

import com.makeappssimple.material.symbols.domain.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.domain.model.MaterialSymbol
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolOptions

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
