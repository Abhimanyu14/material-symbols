package com.makeappssimple.material.symbols.repository

import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight

internal interface MaterialSymbolsRepository {
    suspend fun getAllIcons(): List<MaterialSymbol>

    suspend fun getIconUrl(
        materialSymbol: MaterialSymbol,
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): String

    fun getDrawableResourceFileInfo(
        materialSymbol: MaterialSymbol,
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): DrawableResourceFileInfo
}
