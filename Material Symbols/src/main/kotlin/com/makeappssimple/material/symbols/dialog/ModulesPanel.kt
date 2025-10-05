package com.makeappssimple.material.symbols.dialog

import com.intellij.openapi.ui.ComboBox
import com.makeappssimple.material.symbols.resources.ResourcesProvider
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.JLabel
import javax.swing.JPanel
import org.jetbrains.android.facet.AndroidFacet

internal class ModulesPanel(
    androidFacets: Array<AndroidFacet>,
    initialModule: AndroidFacet?,
    resourcesProvider: ResourcesProvider,
    onModuleChange: (AndroidFacet) -> Unit,
) : JPanel() {
    private val moduleComboBox = ComboBox(androidFacets)

    init {
        layout = FlowLayout(FlowLayout.LEFT)

        add(JLabel(resourcesProvider.moduleLabel))
        moduleComboBox.selectedItem = initialModule
        moduleComboBox.addItemListener { itemEvent ->
            if (itemEvent.stateChange == ItemEvent.SELECTED) {
                onModuleChange(itemEvent.item as AndroidFacet)
            }
        }
        add(moduleComboBox)
    }
}
