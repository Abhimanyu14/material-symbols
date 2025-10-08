package com.makeappssimple.material.symbols.presentation.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.makeappssimple.material.symbols.presentation.dialog.MaterialSymbolsDialog

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
