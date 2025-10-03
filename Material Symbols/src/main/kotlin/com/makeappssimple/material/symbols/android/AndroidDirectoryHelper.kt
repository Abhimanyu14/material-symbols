package com.makeappssimple.material.symbols.android

import com.android.tools.idea.projectsystem.SourceProviderManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import org.jetbrains.android.facet.AndroidFacet

/**
 * A helper class to interact with Android project structure.
 * This class should only be instantiated and used when the Android plugin is known to be present.
 */
internal class AndroidDirectoryHelper(
    private val project: Project,
) {
    fun isAndroidPluginInstalled(): Boolean {
        val androidPluginId = PluginId.findId(
            "org.jetbrains.android",
        )
        return androidPluginId != null && PluginManagerCore.isPluginInstalled(
            id = androidPluginId,
        )
    }

    fun getDrawableDirectory(): PsiDirectory? {
        val androidFacet = project.modules.firstNotNullOfOrNull {
            AndroidFacet.getInstance(it)
        } ?: return null
        val sourceProvidersManager = SourceProviderManager.Companion.getInstance(
            facet = androidFacet,
        )
        val resourceDirectoryFile = sourceProvidersManager.sources.resDirectories.firstOrNull() ?: return null
        val psiManager = PsiManager.getInstance(project)
        val resourceDirectory = psiManager.findDirectory(resourceDirectoryFile) ?: return null
        var drawableDirectory: PsiDirectory? = null
        WriteCommandAction.runWriteCommandAction(
            project,
        ) {
            drawableDirectory = resourceDirectory.findSubdirectory("drawable")
                ?: resourceDirectory.createSubdirectory("drawable")
        }
        return drawableDirectory
    }
}
