package com.makeappssimple.material.symbols.network

import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class IconDataSourceImpl : IconDataSource {
    override suspend fun getAllIcons(
        cacheFile: File?,
    ): List<String> {
        val iconFetcher: IconFetcher = IconFetcherImpl(
            cacheFile = cacheFile,
        )
        return suspendCoroutine { continuation ->
            iconFetcher.fetchIconData(
                url = "https://fonts.google.com/metadata/icons?key=material_symbols&incomplete=true",
                callback = object : IconFetchCallback {
                    override fun onFetchSuccess(
                        icons: List<String>,
                    ) {
                        continuation.resume(
                            value = icons,
                        )
                    }

                    override fun onFetchFailure(
                        ioException: IOException,
                    ) {
                        continuation.resumeWithException(
                            exception = IOException(
                                "Fetch failed: ${ioException.message ?: "An error occurred"}",
                            ),
                        )
                    }
                },
            )
        }
    }
}
