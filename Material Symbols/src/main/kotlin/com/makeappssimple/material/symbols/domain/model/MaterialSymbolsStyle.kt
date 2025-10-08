package com.makeappssimple.material.symbols.domain.model

internal enum class MaterialSymbolsStyle(
    val value: String,
) {
    OUTLINED(
        value = "outlined",
    ),
    ROUNDED(
        value = "rounded",
    ),
    SHARP(
        value = "sharp",
    );

    override fun toString(): String {
        return name
            .lowercase()
            .replaceFirstChar {
                it.uppercase()
            }
    }
}
