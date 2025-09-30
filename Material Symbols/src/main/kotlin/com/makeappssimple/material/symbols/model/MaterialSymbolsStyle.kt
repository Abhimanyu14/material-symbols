package com.makeappssimple.material.symbols.model

enum class MaterialSymbolsStyle(
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
