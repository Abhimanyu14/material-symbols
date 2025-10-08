package com.makeappssimple.material.symbols.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IconResponse(
    @SerialName("icons")
    val icons: List<NetworkIcon>,
)
