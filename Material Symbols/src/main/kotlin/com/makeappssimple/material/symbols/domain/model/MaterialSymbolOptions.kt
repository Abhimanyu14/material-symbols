package com.makeappssimple.material.symbols.domain.model

internal data class MaterialSymbolOptions(
    val isFilled: Boolean,
    val grade: MaterialSymbolsGrade,
    val size: MaterialSymbolsSize,
    val style: MaterialSymbolsStyle,
    val weight: MaterialSymbolsWeight,
)
