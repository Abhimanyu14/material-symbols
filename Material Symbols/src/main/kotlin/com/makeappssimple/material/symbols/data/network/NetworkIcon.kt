package com.makeappssimple.material.symbols.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkIcon(
    @SerialName("name")
    val name: String,
    @SerialName("unsupported_families")
    val unsupportedFamilies: List<String> = emptyList(),
)
