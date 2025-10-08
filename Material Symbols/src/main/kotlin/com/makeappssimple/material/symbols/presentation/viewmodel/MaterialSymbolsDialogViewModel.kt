package com.makeappssimple.material.symbols.presentation.viewmodel

import com.makeappssimple.material.symbols.common.DEFAULT_MATERIAL_SYMBOL_FILLED
import com.makeappssimple.material.symbols.data.repository.MaterialSymbolsRepository
import com.makeappssimple.material.symbols.data.repository.MaterialSymbolsRepositoryImpl
import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_GRADE
import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_SIZE
import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_STYLE
import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_WEIGHT
import com.makeappssimple.material.symbols.domain.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.domain.model.MaterialSymbol
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolOptions
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolsWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class MaterialSymbolsDialogViewModel(
    private val materialSymbolsRepository: MaterialSymbolsRepository = MaterialSymbolsRepositoryImpl(),
) {
    // region data
    var filteredMaterialSymbols: List<MaterialSymbol> = emptyList()
    private val _selectedMaterialSymbols: MutableList<MaterialSymbol> = mutableListOf()
    val selectedMaterialSymbols: List<MaterialSymbol> = _selectedMaterialSymbols
    private var allMaterialSymbols: List<MaterialSymbol> = emptyList()
    // endregion

    // region UI state
    private var _materialSymbolOptions: MaterialSymbolOptions = MaterialSymbolOptions(
        isFilled = DEFAULT_MATERIAL_SYMBOL_FILLED,
        grade = DEFAULT_MATERIAL_SYMBOL_GRADE,
        size = DEFAULT_MATERIAL_SYMBOL_SIZE,
        style = DEFAULT_MATERIAL_SYMBOL_STYLE,
        weight = DEFAULT_MATERIAL_SYMBOL_WEIGHT,
    )
    val materialSymbolOptions: MaterialSymbolOptions
        get() = _materialSymbolOptions
    // endregion

    suspend fun getAllIcons(): List<MaterialSymbol> {
        if (allMaterialSymbols.isNotEmpty()) {
            return emptyList()
        }
        allMaterialSymbols = materialSymbolsRepository.getAllIcons()
        filteredMaterialSymbols = allMaterialSymbols
        return allMaterialSymbols
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
        return materialSymbolsRepository.getIconUrl(
            materialSymbol = materialSymbol,
            materialSymbolOptions = materialSymbolOptions,
        )
    }

    fun getSelectedMaterialSymbolsDrawableResourceFileInfoList(): List<DrawableResourceFileInfo> {
        return selectedMaterialSymbols.map { materialSymbol ->
            materialSymbolsRepository.getDrawableResourceFileInfo(
                materialSymbol = materialSymbol,
                materialSymbolOptions = materialSymbolOptions,
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
        _materialSymbolOptions = materialSymbolOptions.copy(
            isFilled = updatedIsFilled,
        )
    }

    fun updateSelectedGrade(
        updatedSelectedGrade: MaterialSymbolsGrade,
    ) {
        _materialSymbolOptions = materialSymbolOptions.copy(
            grade = updatedSelectedGrade,
        )
    }

    fun updateSelectedSize(
        updatedSelectedSize: MaterialSymbolsSize,
    ) {
        _materialSymbolOptions = materialSymbolOptions.copy(
            size = updatedSelectedSize,
        )
    }

    fun updateSelectedStyle(
        updatedSelectedStyle: MaterialSymbolsStyle,
    ) {
        _materialSymbolOptions = materialSymbolOptions.copy(
            style = updatedSelectedStyle,
        )
    }

    fun updateSelectedWeight(
        updatedSelectedWeight: MaterialSymbolsWeight,
    ) {
        _materialSymbolOptions = materialSymbolOptions.copy(
            weight = updatedSelectedWeight,
        )
    }
}
