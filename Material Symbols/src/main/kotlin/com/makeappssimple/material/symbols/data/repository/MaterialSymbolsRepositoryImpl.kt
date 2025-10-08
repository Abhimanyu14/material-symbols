package com.makeappssimple.material.symbols.data.repository

import com.intellij.openapi.application.PathManager
import com.makeappssimple.material.symbols.data.network.IconDataSource
import com.makeappssimple.material.symbols.data.network.IconDataSourceImpl
import com.makeappssimple.material.symbols.domain.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.domain.model.MaterialSymbol
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolOptions
import java.io.BufferedInputStream
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val cacheDirectoryFileName = "material-symbols-icons"

internal class MaterialSymbolsRepositoryImpl(
    private val iconDataSource: IconDataSource = IconDataSourceImpl(),
) : MaterialSymbolsRepository {
    private var allIcons: List<String> = emptyList()
    private var allMaterialSymbols: List<MaterialSymbol> = emptyList()
    private val drawableResourceFileContentCache: MutableMap<String, String> = mutableMapOf()
    private val iconUrlCache: MutableMap<String, String> = mutableMapOf()

    override suspend fun getAllIcons(): List<MaterialSymbol> {
        if (allIcons.isNotEmpty()) {
            return allMaterialSymbols
        }
        return withContext(
            context = Dispatchers.IO,
        ) {
            val cacheDir = File(
                PathManager.getPluginTempPath(),
                cacheDirectoryFileName,
            )
            cacheDir.mkdirs()
            allIcons = iconDataSource.getAllIcons(
                cacheFile = cacheDir,
            )
            allMaterialSymbols = allIcons.map { icon ->
                MaterialSymbol(
                    name = icon,
                    title = getMaterialSymbolTitle(
                        materialSymbol = icon,
                    ),
                )
            }
            allMaterialSymbols
        }
    }

    override suspend fun getIconUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        return withContext(
            context = Dispatchers.Default,
        ) {
            val cacheKey = getMaterialSymbolStateCacheKey(
                materialSymbol = materialSymbol,
                materialSymbolOptions = materialSymbolOptions,
            )
            val iconUrl = iconUrlCache[cacheKey] ?: MaterialSymbolUrlBuilder.createIconUrl(
                materialSymbol = materialSymbol,
                materialSymbolOptions = materialSymbolOptions,
            )
            iconUrlCache[cacheKey] = iconUrl
            iconUrl
        }
    }

    override fun getDrawableResourceFileInfo(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): DrawableResourceFileInfo {
        val fileContent = getDrawableResourceFileContent(
            materialSymbol = materialSymbol,
            materialSymbolOptions = materialSymbolOptions,
        )
        val fileName: String = MaterialSymbolUrlBuilder.getFileName(
            materialSymbol = materialSymbol,
            materialSymbolOptions = materialSymbolOptions,
        )
        return DrawableResourceFileInfo(
            content = fileContent,
            name = fileName,
        )
    }

    private fun getMaterialSymbolTitle(
        materialSymbol: String,
    ): String {
        return materialSymbol
            .replace(
                oldValue = "_",
                newValue = " ",
            )
            .replaceFirstChar { char ->
                char.titlecase()
            }
    }

    private fun getDrawableResourceFileContent(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val fileUrl = MaterialSymbolUrlBuilder.getFileUrl(
            materialSymbol = materialSymbol,
            materialSymbolOptions = materialSymbolOptions,
        )
        return drawableResourceFileContentCache.getOrPut(
            key = fileUrl,
        ) {
            fetchDrawableResourceFileContent(
                fileUrl = fileUrl,
            )
        }
    }

    private fun fetchDrawableResourceFileContent(
        fileUrl: String,
    ): String {
        return BufferedInputStream(
            URI.create(fileUrl).toURL().openStream(),
        ).use {
            it
                .readBytes()
                .toString(
                    charset = Charsets.UTF_8,
                )
        }
    }

    private fun getMaterialSymbolStateCacheKey(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        return "${materialSymbol.name}:${materialSymbolOptions.style.value}:${materialSymbolOptions.weight.value}:${materialSymbolOptions.isFilled}:${materialSymbolOptions.grade.value}:${materialSymbolOptions.size.value}"
    }
}
