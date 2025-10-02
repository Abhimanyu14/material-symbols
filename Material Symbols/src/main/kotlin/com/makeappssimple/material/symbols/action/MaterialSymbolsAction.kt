package com.makeappssimple.material.symbols.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.makeappssimple.material.symbols.dialog.MaterialSymbolsDialog

public class MaterialSymbolsAction : AnAction() {
    override fun actionPerformed(
        anActionEvent: AnActionEvent,
    ) {
        val project = anActionEvent.project
        if (project != null) {
            MaterialSymbolsDialog(
                project = project,
            ).show()
        }
    }
}
