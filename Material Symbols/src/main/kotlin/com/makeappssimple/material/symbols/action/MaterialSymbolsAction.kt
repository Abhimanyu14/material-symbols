package com.makeappssimple.material.symbols.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.makeappssimple.material.symbols.dialog.MaterialSymbolsDialog

public class MaterialSymbolsAction : AnAction() {
    override fun actionPerformed(
        anActionEvent: AnActionEvent,
    ) {
        anActionEvent.project?.let { project ->
            MaterialSymbolsDialog(
                project = project,
            ).show()
        }
    }
}
