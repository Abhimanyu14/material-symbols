package com.makeappssimple.material.symbols.domain.model

internal enum class MaterialSymbolsSize(
    val value: Int,
) {
    S20(
        value = 20,
    ),
    S24(
        value = 24,
    ),
    S40(
        value = 40,
    ),
    S48(
        value = 48,
    );

    override fun toString(): String {
        return "$value dp"
    }
}

internal val DEFAULT_MATERIAL_SYMBOL_SIZE = MaterialSymbolsSize.S24
