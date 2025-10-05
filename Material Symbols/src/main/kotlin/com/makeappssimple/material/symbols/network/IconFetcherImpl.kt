package com.makeappssimple.material.symbols.network

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

private object IconFetcherImplConstants {
    const val FIVE_MB_IN_BYTES = 5 * 1024 * 1024.toLong()
}

internal class IconFetcherImpl(
    private val cacheFile: File? = null,
    private val iconParser: IconParser = IconParserImpl(),
) : IconFetcher {
    override fun fetchIconData(
        callback: IconFetchCallback,
        url: String,
    ) {
        val okHttpClientBuilder = OkHttpClient()
            .newBuilder()
            .cache(
                cache = cacheFile?.run {
                    Cache(
                        directory = cacheFile,
                        maxSize = IconFetcherImplConstants.FIVE_MB_IN_BYTES,
                    )
                },
            )

        val okHttpClient = okHttpClientBuilder.build()
        val request = Request.Builder()
            .cacheControl(
                cacheControl = CacheControl.Builder()
                    .minFresh(
                        minFresh = 3,
                        timeUnit = TimeUnit.DAYS,
                    )
                    .maxStale(
                        maxStale = 30,
                        timeUnit = TimeUnit.DAYS,
                    )
                    .build(),
            )
            .url(
                url = url,
            )
            .build()

        okHttpClient
            .newCall(
                request = request,
            ).enqueue(
                responseCallback = object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        callback.onFetchFailure(
                            ioException = e,
                        )
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        callback.onFetchSuccess(
                            icons = iconParser.parseIconData(
                                data = response.body?.string().orEmpty(),
                            ),
                        )
                    }
                },
            )
    }
}
