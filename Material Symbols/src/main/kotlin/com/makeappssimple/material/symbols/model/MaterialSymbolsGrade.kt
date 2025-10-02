package com.makeappssimple.material.symbols.model

internal enum class MaterialSymbolsGrade(
    val value: Int,
) {
    GRADE_NEGATIVE_25(
        value = -25,
    ),
    GRADE_0(
        value = 0,
    ),
    GRADE_200(
        value = 200,
    );

    override fun toString(): String {
        return value.toString()
    }
}
