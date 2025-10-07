package com.makeappssimple.material.symbols.viewmodel

import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import com.makeappssimple.material.symbols.model.MaterialSymbol
import com.makeappssimple.material.symbols.model.MaterialSymbolsGrade
import com.makeappssimple.material.symbols.model.MaterialSymbolsSize
import com.makeappssimple.material.symbols.model.MaterialSymbolsStyle
import com.makeappssimple.material.symbols.model.MaterialSymbolsWeight
import com.makeappssimple.material.symbols.repository.DEFAULT_FILLED
import com.makeappssimple.material.symbols.repository.DEFAULT_GRADE
import com.makeappssimple.material.symbols.repository.DEFAULT_SIZE
import com.makeappssimple.material.symbols.repository.DEFAULT_STYLE
import com.makeappssimple.material.symbols.repository.DEFAULT_WEIGHT
import com.makeappssimple.material.symbols.repository.MaterialSymbolsRepository
import com.makeappssimple.material.symbols.repository.MaterialSymbolsRepositoryImpl
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
            isFilled = isFilled,
            grade = selectedGrade,
            size = selectedSize,
            style = selectedStyle,
            weight = selectedWeight,
        )
    }

    fun getSelectedMaterialSymbolsDrawableResourceFileInfoList(): List<DrawableResourceFileInfo> {
        return selectedMaterialSymbols.map { materialSymbol ->
            materialSymbolsRepository.getDrawableResourceFileInfo(
                materialSymbol = materialSymbol,
                isFilled = isFilled,
                grade = selectedGrade,
                size = selectedSize,
                style = selectedStyle,
                weight = selectedWeight,
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
}
