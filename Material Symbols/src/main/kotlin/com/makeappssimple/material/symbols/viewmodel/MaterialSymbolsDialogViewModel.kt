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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Default values for URL construction
private const val DEFAULT_FILLED = false
private val DEFAULT_GRADE = MaterialSymbolsGrade.GRADE_0
private val DEFAULT_SIZE = MaterialSymbolsSize.S24
private val DEFAULT_STYLE = MaterialSymbolsStyle.ROUNDED
private val DEFAULT_WEIGHT = MaterialSymbolsWeight.W400

private const val cacheDirectoryFileName = "material-symbols-icons"

internal class MaterialSymbolsDialogViewModel() {
    // region data
    var filteredMaterialSymbols: List<MaterialSymbol> = emptyList()
    private val _selectedMaterialSymbols: MutableList<MaterialSymbol> = mutableListOf()
    val selectedMaterialSymbols: List<MaterialSymbol> = _selectedMaterialSymbols
    private val iconDataSource: IconDataSource = IconDataSourceImpl()
    private var allIcons: List<String> = emptyList()
    private var allMaterialSymbols: List<MaterialSymbol> = emptyList()
    private val drawableResourceFileContentCache: MutableMap<String, String> = mutableMapOf()
    private val iconUrlCache: MutableMap<String, String> = mutableMapOf()
    // endregion

    // region UI state
    private var _isFilled: Boolean = DEFAULT_FILLED
    val isFilled: Boolean
        get() = _isFilled
    private var _selectedGrade: MaterialSymbolsGrade = DEFAULT_GRADE
    val selectedGrade: MaterialSymbolsGrade
        get() = _selectedGrade
    private var _selectedSize: MaterialSymbolsSize = DEFAULT_SIZE
    val selectedSize: MaterialSymbolsSize
        get() = _selectedSize
    private var _selectedStyle: MaterialSymbolsStyle = DEFAULT_STYLE
    val selectedStyle: MaterialSymbolsStyle
        get() = _selectedStyle
    private var _selectedWeight: MaterialSymbolsWeight = DEFAULT_WEIGHT
    val selectedWeight: MaterialSymbolsWeight
        get() = _selectedWeight
    // endregion

    suspend fun getAllIcons(): List<MaterialSymbol> {
        if (allIcons.isNotEmpty()) {
            return emptyList()
        }
        return withContext(
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
            allMaterialSymbols
        }
    }

    suspend fun updateFilteredMaterialSymbols(
        searchText: String,
    ) {
        withContext(
            context = Dispatchers.Default,
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
    }

    suspend fun getIconUrl(
        materialSymbol: MaterialSymbol,
    ): String {
        return withContext(
            context = Dispatchers.Default,
        ) {
            val cacheKey = getMaterialSymbolStateCacheKey(
                materialSymbol = materialSymbol,
            )
            val iconUrl = iconUrlCache[cacheKey] ?: createIconUrl(
                materialSymbol = materialSymbol,
            )
            iconUrlCache[cacheKey] = iconUrl
            iconUrl
        }
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
        _selectedMaterialSymbols.add(
            element = materialSymbol,
        )
    }

    fun removeFromSelectedMaterialSymbols(
        materialSymbol: MaterialSymbol,
    ) {
        _selectedMaterialSymbols.remove(
            element = materialSymbol,
        )
    }

    fun updateIsFilled(
        updatedIsFilled: Boolean,
    ) {
        _isFilled = updatedIsFilled
    }

    fun updateSelectedGrade(
        updatedSelectedGrade: MaterialSymbolsGrade,
    ) {
        _selectedGrade = updatedSelectedGrade
    }

    fun updateSelectedSize(
        updatedSelectedSize: MaterialSymbolsSize,
    ) {
        _selectedSize = updatedSelectedSize
    }

    fun updateSelectedStyle(
        updatedSelectedStyle: MaterialSymbolsStyle,
    ) {
        _selectedStyle = updatedSelectedStyle
    }

    fun updateSelectedWeight(
        updatedSelectedWeight: MaterialSymbolsWeight,
    ) {
        _selectedWeight = updatedSelectedWeight
    }

    private fun createIconUrl(
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
