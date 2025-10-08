package com.makeappssimple.material.symbols.presentation.dialog

import com.intellij.openapi.ui.ComboBox
import com.makeappssimple.material.symbols.presentation.resources.ResourcesProvider
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.JLabel
import javax.swing.JPanel
import org.jetbrains.android.facet.AndroidFacet

internal class ModulesPanel(
    private val initialModule: AndroidFacet?,
    private val androidModules: Array<AndroidFacet>,
    private val resourcesProvider: ResourcesProvider,
    private val onModuleChange: (AndroidFacet) -> Unit,
) : JPanel() {
    init {
        initModuleComboBoxUI()
    }

    override fun getMaximumSize(): Dimension {
        return Dimension(
            super.getMaximumSize().width,
            preferredSize.height,
        )
    }

    private fun initModuleComboBoxUI() {
        val moduleComboBox = ComboBox(androidModules)
        moduleComboBox.selectedItem = initialModule
        moduleComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onModuleChange(itemEvent.item as AndroidFacet)
            }
        }
        add(JLabel(resourcesProvider.moduleLabel))
        add(moduleComboBox)
    }
}
