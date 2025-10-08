package com.makeappssimple.material.symbols.data.network

import com.makeappssimple.material.symbols.common.MATERIAL_SYMBOLS_METADATA_URL
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
                url = MATERIAL_SYMBOLS_METADATA_URL,
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
