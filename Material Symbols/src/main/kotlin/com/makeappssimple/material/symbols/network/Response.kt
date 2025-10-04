package com.makeappssimple.material.symbols.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IconResponse(
    @SerialName("icons")
    val icons: List<NetworkIcon>,
)

@Serializable
internal data class NetworkIcon(
    @SerialName("name")
    val name: String,
    @SerialName("unsupported_families")
    val unsupportedFamilies: List<String> = emptyList(),
)
