package com.makeappssimple.material.symbols.dialog

import com.intellij.ui.SearchTextField
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal class SearchBar(
    private val onSearchTextUpdate: (String) -> Unit,
) : JPanel() {
    private val searchTextField = SearchTextField()

    init {
        initUI()
        initListeners()
    }

    private fun initUI() {
        layout = BorderLayout()

        add(searchTextField, BorderLayout.NORTH)
    }

    private fun initListeners() {
        searchTextField.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(searchTextField.text)
                }

                override fun removeUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(searchTextField.text)
                }

                override fun changedUpdate(
                    e: DocumentEvent?,
                ) {
                    onSearchTextUpdate(searchTextField.text)
                }
            },
        )
    }
}
