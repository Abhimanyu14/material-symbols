package com.makeappssimple.material.symbols.repository

import com.intellij.openapi.application.PathManager
import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.model.MaterialSymbol
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

internal class MaterialSymbolsRepositoryImpl : MaterialSymbolsRepository {
    private val iconDataSource: IconDataSource = IconDataSourceImpl()
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
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): String {
        return withContext(
            context = Dispatchers.Default,
        ) {
            val cacheKey = getMaterialSymbolStateCacheKey(
                materialSymbol = materialSymbol,
                isFilled = isFilled,
                grade = grade,
                size = size,
                style = style,
                weight = weight,
            )
            val iconUrl = iconUrlCache[cacheKey] ?: createIconUrl(
                materialSymbol = materialSymbol,
                isFilled = isFilled,
                grade = grade,
                size = size,
                style = style,
            )
            iconUrlCache[cacheKey] = iconUrl
            iconUrl
        }
    }

    override fun getDrawableResourceFileInfo(
        materialSymbol: MaterialSymbol,
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): DrawableResourceFileInfo {
        val fileContent = getDrawableResourceFileContent(
            materialSymbol = materialSymbol,
            isFilled = isFilled,
            grade = grade,
            size = size,
            style = style,
            weight = weight,
        )
        val fileName: String = getFileName(
            materialSymbol = materialSymbol,
            isFilled = isFilled,
            grade = grade,
            size = size,
            style = style,
            weight = weight,
        )
        return DrawableResourceFileInfo(
            content = fileContent,
            name = fileName,
        )
    }

    private fun createIconUrl(
        materialSymbol: MaterialSymbol,
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
    ): String {
        val styleString = "materialsymbols${style.value}"
        val options = mutableListOf<String>()
        if (grade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (grade.value < 0) {
                        "N${-grade.value}"
                    } else {
                        grade.value
                    }
                }",
            )
        }
        if (isFilled) {
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${size.value}px.svg"
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
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): String {
        val fileUrl = getFileUrl(
            materialSymbol = materialSymbol,
            isFilled = isFilled,
            grade = grade,
            size = size,
            style = style,
            weight = weight,
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
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): String {
        val styleString = "materialsymbols${style.value}"
        val options = mutableListOf<String>()
        if (weight != DEFAULT_WEIGHT) {
            options.add(
                element = "wght${weight.value}",
            )
        }
        if (grade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (grade.value < 0) {
                        "N${-grade.value}"
                    } else {
                        grade.value
                    }
                }",
            )
        }
        if (isFilled) {
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${size.value}px.xml"
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
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
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
        val styleValue = "_${style.value}"
        val weightValue = if (weight != DEFAULT_WEIGHT) {
            "_w${weight.value}"
        } else {
            ""
        }
        val filledValue = if (isFilled) {
            "_filled"
        } else {
            ""
        }
        val gradeValue = if (grade != DEFAULT_GRADE) {
            grade.value.let {
                if (it < 0) {
                    "_gn${-it}"
                } else {
                    "_g$it"
                }
            }
        } else {
            ""
        }
        val sizeValue = "_${size.value}dp"
        return "ic_${sanitizedFileName}${styleValue}${weightValue}${filledValue}${gradeValue}${sizeValue}.xml"
    }

    private fun getMaterialSymbolStateCacheKey(
        materialSymbol: MaterialSymbol,
        isFilled: Boolean,
        grade: MaterialSymbolsGrade,
        size: MaterialSymbolsSize,
        style: MaterialSymbolsStyle,
        weight: MaterialSymbolsWeight,
    ): String {
        return "${materialSymbol.name}:${style.value}:${weight.value}::${isFilled}${grade.value}:${size.value}"
    }
}
