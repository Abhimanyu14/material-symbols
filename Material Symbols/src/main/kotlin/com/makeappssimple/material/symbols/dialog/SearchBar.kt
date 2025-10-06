package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.SearchTextField
import java.awt.Dimension
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private const val searchBarHeight = 30

internal class SearchBar(
    private val onSearchTextUpdate: (String) -> Unit,
) : SearchTextField() {
    init {
        initListeners()
    }

    override fun getMaximumSize(): Dimension? {
        val size = super.getMaximumSize()
        return Dimension(size.width, searchBarHeight)
    }

    override fun getMinimumSize(): Dimension? {
        val size = super.getMinimumSize()
        return Dimension(size.width, searchBarHeight)
    }

    override fun getPreferredSize(): Dimension? {
        val size = super.getPreferredSize()
        return Dimension(size.width, searchBarHeight)
    }

    override fun getSize(rv: Dimension?): Dimension? {
        val size = super.getSize()
        return Dimension(size.width, searchBarHeight)
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
