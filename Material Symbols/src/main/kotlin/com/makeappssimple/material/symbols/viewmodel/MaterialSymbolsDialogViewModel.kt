package com.makeappssimple.material.symbols.viewmodel

import com.intellij.openapi.application.PathManager
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.network.IconDataSource
import com.makeappssimple.material.symbols.network.IconDataSourceImpl
import java.io.BufferedInputStream
import java.io.File
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

// Default values for URL construction
private val DEFAULT_STYLE = MaterialSymbolsStyle.ROUNDED
private const val DEFAULT_FILL = false
private val DEFAULT_WEIGHT = MaterialSymbolsWeight.W400
private val DEFAULT_GRADE = MaterialSymbolsGrade.GRADE_0
private val DEFAULT_SIZE = MaterialSymbolsSize.S24

private const val cacheDirectoryFileName = "material-symbols-icons"

internal class MaterialSymbolsDialogViewModel(
    private val coroutineScope: CoroutineScope,
) {
    // region data
    val iconDataSource: IconDataSource = IconDataSourceImpl()
    var allMaterialSymbols: List<MaterialSymbol> = emptyList()
    val selectedMaterialSymbols = mutableListOf<MaterialSymbol>()
    private val drawableResourceFileContentCache = mutableMapOf<String, String>()
    // endregion

    // region UI state
    var selectedStyle: MaterialSymbolsStyle = DEFAULT_STYLE
    var selectedFill: Boolean = DEFAULT_FILL
    var selectedWeight: MaterialSymbolsWeight = DEFAULT_WEIGHT
    var selectedGrade: MaterialSymbolsGrade = DEFAULT_GRADE
    var selectedSize: MaterialSymbolsSize = DEFAULT_SIZE
    // endregion

    suspend fun getAllIcons() {
        if (allMaterialSymbols.isNotEmpty()) {
            return
        }
        withContext(
            Dispatchers.IO,
        ) {
            val cacheDir = File(
                PathManager.getPluginTempPath(),
                cacheDirectoryFileName,
            )
            cacheDir.mkdirs()
            val allIcons = iconDataSource.getAllIcons(
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
        }
    }

    fun getMaterialSymbolTitle(
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

    fun getDrawableResourceFileContent(
        materialSymbol: MaterialSymbol,
    ): String {
        val fileUrl = getFileUrl(
            materialSymbol = materialSymbol,
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
    ): String {
        val styleString = "materialsymbols${selectedStyle.value}"
        val options = mutableListOf<String>()
        if (selectedWeight != DEFAULT_WEIGHT) {
            options.add(
                element = "wght${selectedWeight.value}",
            )
        }
        if (selectedGrade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (selectedGrade.value < 0) {
                        "N${-selectedGrade.value}"
                    } else {
                        selectedGrade.value
                    }
                }",
            )
        }
        if (selectedFill) {
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${selectedSize.value}px.xml"
        return url
    }

    private fun fetchDrawableResourceFileContent(
        fileUrl: String,
    ): String {
        return BufferedInputStream(
            URL(
                fileUrl,
            ).openStream(),
        ).use {
            it
                .readBytes()
                .toString(
                    charset = Charsets.UTF_8,
                )
        }
    }

    fun getIconUrl(
        materialSymbol: MaterialSymbol,
    ): String {
        val styleString = "materialsymbols${selectedStyle.value}"
        val options = mutableListOf<String>()
        if (selectedGrade != DEFAULT_GRADE) {
            options.add(
                element = "grad${
                    if (selectedGrade.value < 0) {
                        "N${-selectedGrade.value}"
                    } else {
                        selectedGrade.value
                    }
                }",
            )
        }
        if (selectedFill) {
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${selectedSize.value}px.svg"
        return url
    }

    fun getFileName(
        materialSymbol: MaterialSymbol,
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
        val style = "_${selectedStyle.value}"
        val weight = if (selectedWeight != DEFAULT_WEIGHT) {
            "_w${selectedWeight.value}"
        } else {
            ""
        }
        val fill = if (selectedFill) {
            "_filled"
        } else {
            ""
        }
        val grade = if (selectedGrade != DEFAULT_GRADE) {
            selectedGrade.value.let {
                if (it < 0) {
                    "_gn${-it}"
                } else {
                    "_g$it"
                }
            }
        } else {
            ""
        }
        val size = "_${selectedSize.value}dp"
        return "ic_${sanitizedFileName}${style}${weight}${fill}${grade}${size}.xml"
    }

    fun dispose() {
        coroutineScope.cancel()
    }
}
