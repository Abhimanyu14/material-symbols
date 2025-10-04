package com.makeappssimple.material.symbols.network

import kotlinx.serialization.json.Json

internal class IconParserImpl : IconParser {
    private val json = Json { ignoreUnknownKeys = true }

    override fun parseIconData(
        data: String,
    ): List<String> {
        // The response starts with `)]}'` which is not valid JSON, so we need to remove it.
        val cleanData = data.removePrefix(")]}'")
        val iconResponse = json.decodeFromString<IconResponse>(cleanData)
        return iconResponse.icons
            .filterNot { it.unsupportedFamilies.contains("Material Symbols Rounded") }
            .map { it.name }
    }
}
