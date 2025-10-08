package com.makeappssimple.material.symbols.data.network

import java.io.IOException

internal interface IconFetcher {
    fun fetchIconData(
        callback: IconFetchCallback,
        url: String,
    )
}

internal interface IconFetchCallback {
    fun onFetchSuccess(icons: List<String>)
    fun onFetchFailure(ioException: IOException)
}
