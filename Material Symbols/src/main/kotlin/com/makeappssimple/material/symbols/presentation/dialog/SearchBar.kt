package com.makeappssimple.material.symbols.presentation.dialog

import com.intellij.ui.SearchTextField
import java.awt.Dimension
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal class SearchBar(
    private val onSearchTextUpdate: (String) -> Unit,
) : SearchTextField() {
    init {
        initListeners()
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(super.getMaximumSize().width, preferredSize.height)
    }

    private fun initListeners() {
        addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(text)
                }

                override fun removeUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(text)
                }

                override fun changedUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(text)
                }
            },
        )
    }
}
