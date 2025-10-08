package com.makeappssimple.material.symbols.repository

import com.intellij.openapi.application.PathManager
import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolOptions
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.network.IconDataSource
import com.makeappssimple.material.symbols.network.IconDataSourceImpl
import java.io.BufferedInputStream
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Default values for URL construction
internal const val DEFAULT_FILLED = false
internal val DEFAULT_GRADE = MaterialSymbolsGrade.GRADE_0
internal val DEFAULT_SIZE = MaterialSymbolsSize.S24
internal val DEFAULT_STYLE = MaterialSymbolsStyle.ROUNDED
internal val DEFAULT_WEIGHT = MaterialSymbolsWeight.W400

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
            val iconUrl = iconUrlCache[cacheKey] ?: createIconUrl(
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
        val fileName: String = getFileName(
            materialSymbol = materialSymbol,
            materialSymbolOptions = materialSymbolOptions,
        )
        return DrawableResourceFileInfo(
            content = fileContent,
            name = fileName,
        )
    }

    private fun createIconUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val styleString = "materialsymbols${materialSymbolOptions.style.value}"
        val options = mutableListOf<String>()
        if (materialSymbolOptions.grade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (materialSymbolOptions.grade.value < 0) {
                        "N${-materialSymbolOptions.grade.value}"
                    } else {
                        materialSymbolOptions.grade.value
                    }
                }",
            )
        }
        if (materialSymbolOptions.isFilled) {
            options.add(
                element = "fill1",
            )
        }
        val optionsString = if (options.isEmpty()) {
            "default"
        } else {
            options.joinToString(
                separator = "",
            )
        }
        val url =
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${materialSymbolOptions.size.value}px.svg"
        return url
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
        val fileUrl = getFileUrl(
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

    private fun getFileUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val styleString = "materialsymbols${materialSymbolOptions.style.value}"
        val options = mutableListOf<String>()
        if (materialSymbolOptions.weight != DEFAULT_WEIGHT) {
            options.add(
                element = "wght${materialSymbolOptions.weight.value}",
            )
        }
        if (materialSymbolOptions.grade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (materialSymbolOptions.grade.value < 0) {
                        "N${-materialSymbolOptions.grade.value}"
                    } else {
                        materialSymbolOptions.grade.value
                    }
                }",
            )
        }
        if (materialSymbolOptions.isFilled) {
            options.add(
                element = "fill1",
            )
        }
        val optionsString = if (options.isEmpty()) {
            "default"
        } else {
            options.joinToString(
                separator = "",
            )
        }
        val url =
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${materialSymbolOptions.size.value}px.xml"
        return url
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

    private fun getFileName(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val sanitizedFileName = materialSymbol.name
            .lowercase()
            .replace(
                oldValue = " ",
                newValue = "_",
            )
            .replace(
                regex = Regex(
                    pattern = "[^a-z0-9_]",
                ),
                replacement = "",
            )
        val styleValue = "_${materialSymbolOptions.style.value}"
        val weightValue = if (materialSymbolOptions.weight != DEFAULT_WEIGHT) {
            "_w${materialSymbolOptions.weight.value}"
        } else {
            ""
        }
        val filledValue = if (materialSymbolOptions.isFilled) {
            "_filled"
        } else {
            ""
        }
        val gradeValue = if (materialSymbolOptions.grade != DEFAULT_GRADE) {
            materialSymbolOptions.grade.value.let {
                if (it < 0) {
                    "_gn${-it}"
                } else {
                    "_g$it"
                }
            }
        } else {
            ""
        }
        val sizeValue = "_${materialSymbolOptions.size.value}dp"
        return "ic_${sanitizedFileName}${styleValue}${weightValue}${filledValue}${gradeValue}${sizeValue}.xml"
    }

    private fun getMaterialSymbolStateCacheKey(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        return "${materialSymbol.name}:${materialSymbolOptions.style.value}:${materialSymbolOptions.weight.value}:${materialSymbolOptions.isFilled}:${materialSymbolOptions.grade.value}:${materialSymbolOptions.size.value}"
    }
}
