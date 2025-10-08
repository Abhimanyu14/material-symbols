package com.makeappssimple.material.symbols.presentation.cache

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SvgDocumentCache() {
    private val svgDocumentCache = ConcurrentHashMap<String, SVGDocument?>()

    suspend fun getSvgDocument(
        iconUrl: String,
    ): SVGDocument? {
        return withContext(
            context = Dispatchers.IO,
        ) {
            svgDocumentCache[iconUrl] ?: fetchAndCacheSvgDocument(
                iconUrl = iconUrl,
            )
        }
    }

    private fun fetchAndCacheSvgDocument(
        iconUrl: String,
    ): SVGDocument? {
        return try {
            val svgUrl = URI.create(iconUrl).toURL() ?: return null
            val svgLoader = SVGLoader()
            val svgDocument: SVGDocument = svgLoader.load(svgUrl) ?: return null
            cacheSvgDocument(
                iconUrl = iconUrl,
                svgDocument = svgDocument,
            )
            svgDocument
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

    private fun cacheSvgDocument(
        iconUrl: String,
        svgDocument: SVGDocument,
    ) {
        svgDocumentCache[iconUrl] = svgDocument
    }
}
