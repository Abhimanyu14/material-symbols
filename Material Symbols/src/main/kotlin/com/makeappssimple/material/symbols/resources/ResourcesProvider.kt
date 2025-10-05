package com.makeappssimple.material.symbols.resources

import java.util.ResourceBundle

internal class ResourcesProvider {
    private val resourceBundle = ResourceBundle.getBundle("strings")

    val dialogTitle: String = resourceBundle.getString("dialog.title")
    val dialogProgress: String = resourceBundle.getString("dialog.progress")
    val errorErrorTitle: String = resourceBundle.getString("dialog.error.title")

    val downloadErrorPrefix: String = resourceBundle.getString("dialog.error.download.prefix")
    val loadErrorPrefix: String = resourceBundle.getString("dialog.error.load.prefix")

    val cellError: String = resourceBundle.getString("cell.error")

    val filledLabel: String = resourceBundle.getString("options.filled")
    val styleLabel: String = resourceBundle.getString("options.style")
    val sizeLabel: String = resourceBundle.getString("options.size")
    val weightLabel: String = resourceBundle.getString("options.weight")
    val gradeLabel: String = resourceBundle.getString("options.grade")

    val moduleLabel: String = resourceBundle.getString("module")
}
