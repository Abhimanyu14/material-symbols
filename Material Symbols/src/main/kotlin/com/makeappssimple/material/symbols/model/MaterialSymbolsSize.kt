package com.makeappssimple.material.symbols.model

enum class MaterialSymbolsSize(
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
        return "${value} dp"
    }
}
