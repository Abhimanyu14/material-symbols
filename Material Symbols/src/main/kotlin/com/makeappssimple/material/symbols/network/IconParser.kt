package com.makeappssimple.material.symbols.network

internal interface IconParser {
    fun parseIconData(
        data: String,
    ): List<String>
}
