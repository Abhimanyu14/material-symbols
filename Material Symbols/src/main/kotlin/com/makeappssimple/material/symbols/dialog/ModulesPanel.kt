package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.ui.ComboBox
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import java.awt.FlowLayout
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
