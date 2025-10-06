package com.makeappssimple.material.symbols.cache

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import java.net.URI
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SvgDocumentCache() {
    private val svgDocumentCache = ConcurrentHashMap<String, SVGDocument?>()

    suspend fun getSvgDocument(
        iconUrl: String,
    ): SVGDocument? {
        return svgDocumentCache[iconUrl] ?: fetchAndCacheSvgDocument(
            iconUrl = iconUrl,
        )
    }

    private suspend fun fetchAndCacheSvgDocument(
        iconUrl: String,
    ): SVGDocument? {
        return withContext(
            context = Dispatchers.IO,
        ) {
            try {
                val svgUrl = URI.create(iconUrl).toURL()
                val svgLoader = SVGLoader()
                val svgDocument: SVGDocument? = svgLoader.load(
                    Objects.requireNonNull(svgUrl, "SVG file not found"),
                )
                svgDocument?.also {
                    cacheSvgDocument(
                        iconUrl = iconUrl,
                        svgDocument = svgDocument,
                    )
                }
            } catch (
                cancellationException: CancellationException,
            ) {
                throw cancellationException
            } catch (
                _: Exception,
            ) {
                null
            }
        }
    }

    private fun cacheSvgDocument(
        iconUrl: String,
        svgDocument: SVGDocument,
    ) {
        svgDocumentCache[iconUrl] = svgDocument
    }
}
