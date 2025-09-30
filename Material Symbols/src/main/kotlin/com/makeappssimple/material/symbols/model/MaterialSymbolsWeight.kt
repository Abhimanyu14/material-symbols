package com.makeappssimple.material.symbols.model

enum class MaterialSymbolsWeight(
    val value: Int,
) {
    W100(
        value = 100,
    ),
    W200(
        value = 200,
    ),
    W300(
        value = 300,
    ),
    W400(
        value = 400,
    ),
    W500(
        value = 500,
    ),
    W600(
        value = 600,
    ),
    W700(
        value = 700,
    );

    override fun toString(): String {
        return value.toString()
    }
}
