package com.makeappssimple.material.symbols.data.network

internal interface IconParser {
    fun parseIconData(
        data: String,
    ): List<String>
}
