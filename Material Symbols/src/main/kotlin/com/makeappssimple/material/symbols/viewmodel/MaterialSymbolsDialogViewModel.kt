package com.makeappssimple.material.symbols.viewmodel

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Default values for URL construction
private const val DEFAULT_FILLED = false
private val DEFAULT_GRADE = MaterialSymbolsGrade.GRADE_0
private val DEFAULT_SIZE = MaterialSymbolsSize.S24
private val DEFAULT_STYLE = MaterialSymbolsStyle.ROUNDED
private val DEFAULT_WEIGHT = MaterialSymbolsWeight.W400

private const val cacheDirectoryFileName = "material-symbols-icons"

internal class MaterialSymbolsDialogViewModel(
    private val coroutineScope: CoroutineScope,
) {
    // region data
    var filteredMaterialSymbols: List<MaterialSymbol> = emptyList()
    val selectedMaterialSymbols: MutableList<MaterialSymbol> = mutableListOf()
    private val iconDataSource: IconDataSource = IconDataSourceImpl()
    private var allIcons: List<String> = emptyList()
    private var allMaterialSymbols: List<MaterialSymbol> = emptyList()
    private val drawableResourceFileContentCache: MutableMap<String, String> = mutableMapOf()
    private val iconUrlCache: MutableMap<String, String> = mutableMapOf()
    // endregion

    // region UI state
    var isFilled: Boolean = DEFAULT_FILLED
    var selectedGrade: MaterialSymbolsGrade = DEFAULT_GRADE
    var selectedSize: MaterialSymbolsSize = DEFAULT_SIZE
    var selectedStyle: MaterialSymbolsStyle = DEFAULT_STYLE
    var selectedWeight: MaterialSymbolsWeight = DEFAULT_WEIGHT
    // endregion

    suspend fun fetchAllIcons() {
        if (allIcons.isNotEmpty()) {
            return
        }
        withContext(
            context = Dispatchers.IO,
        ) {
            val cacheDir = File(PathManager.getPluginTempPath(), cacheDirectoryFileName)
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
            filteredMaterialSymbols = allMaterialSymbols
        }
    }

    fun updateFilteredMaterialSymbols(
        searchText: String,
    ) {
        filteredMaterialSymbols = if (searchText.isBlank()) {
            allMaterialSymbols
        } else {
            allMaterialSymbols.filter { materialSymbol ->
                materialSymbol.title.contains(
                    other = searchText,
                    ignoreCase = true,
                )
            }
        }
    }

    fun getIconUrl(
        materialSymbol: MaterialSymbol,
    ): String {
        val cacheKey = getMaterialSymbolStateCacheKey(
            materialSymbol = materialSymbol,
        )
        iconUrlCache[cacheKey]?.let {
            return it
        }
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${selectedSize.value}px.svg"
        iconUrlCache[cacheKey] = url
        return url
    }

    fun getSelectedMaterialSymbolsDrawableResourceFileInfoList(): List<DrawableResourceFileInfo> {
        return selectedMaterialSymbols.map { materialSymbol ->
            getDrawableResourceFileInfo(
                materialSymbol = materialSymbol,
            )
        }
    }

    fun addToSelectedMaterialSymbols(
        materialSymbol: MaterialSymbol,
    ) {
        selectedMaterialSymbols.add(
            element = materialSymbol,
        )
    }

    fun removeFromSelectedMaterialSymbols(
        materialSymbol: MaterialSymbol,
    ) {
        selectedMaterialSymbols.remove(
            element = materialSymbol,
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
            "https://fonts.gstatic.com/s/i/short-term/release/${styleString}/${materialSymbol.name}/${optionsString}/${selectedSize.value}px.xml"
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
        val filledValue = if (isFilled) {
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
        return "ic_${sanitizedFileName}${style}${weight}${filledValue}${grade}${size}.xml"
    }

    private fun getDrawableResourceFileInfo(
        materialSymbol: MaterialSymbol,
    ): DrawableResourceFileInfo {
        val fileContent = getDrawableResourceFileContent(
            materialSymbol = materialSymbol,
        )
        val fileName: String = getFileName(
            materialSymbol = materialSymbol,
        )
        return DrawableResourceFileInfo(
            content = fileContent,
            name = fileName,
        )
    }

    private fun getMaterialSymbolStateCacheKey(
        materialSymbol: MaterialSymbol,
    ): String {
        return "${materialSymbol.name}:${selectedStyle.value}:${selectedWeight.value}::${isFilled}${selectedGrade.value}:${selectedSize.value}"
    }
}
