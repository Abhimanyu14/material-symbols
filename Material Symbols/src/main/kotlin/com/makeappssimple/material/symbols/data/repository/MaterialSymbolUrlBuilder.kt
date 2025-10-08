package com.makeappssimple.material.symbols.data.repository

import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_GRADE
import com.makeappssimple.material.symbols.domain.model.DEFAULT_MATERIAL_SYMBOL_WEIGHT
import com.makeappssimple.material.symbols.domain.model.MaterialSymbol
import com.makeappssimple.material.symbols.domain.model.MaterialSymbolOptions

internal object MaterialSymbolUrlBuilder {
    fun createIconUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val styleString = "materialsymbols${materialSymbolOptions.style.value}"
        val options = mutableListOf<String>()
        if (materialSymbolOptions.grade != DEFAULT_MATERIAL_SYMBOL_GRADE) {
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

    fun getFileUrl(
        materialSymbol: MaterialSymbol,
        materialSymbolOptions: MaterialSymbolOptions,
    ): String {
        val styleString = "materialsymbols${materialSymbolOptions.style.value}"
        val options = mutableListOf<String>()
        if (materialSymbolOptions.weight != DEFAULT_MATERIAL_SYMBOL_WEIGHT) {
            options.add(
                element = "wght${materialSymbolOptions.weight.value}",
            )
        }
        if (materialSymbolOptions.grade != DEFAULT_MATERIAL_SYMBOL_GRADE) {
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

    fun getFileName(
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
        val weightValue = if (materialSymbolOptions.weight != DEFAULT_MATERIAL_SYMBOL_WEIGHT) {
            "_w${materialSymbolOptions.weight.value}"
        } else {
            ""
        }
        val filledValue = if (materialSymbolOptions.isFilled) {
            "_filled"
        } else {
            ""
        }
        val gradeValue = if (materialSymbolOptions.grade != DEFAULT_MATERIAL_SYMBOL_GRADE) {
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
}
