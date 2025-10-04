package com.makeappssimple.material.symbols.android

import com.android.tools.idea.projectsystem.SourceProviderManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.makeappssimple.material.symbols.model.DrawableResourceFileInfo
import org.jetbrains.android.facet.AndroidFacet

/**
 * A helper class to interact with Android project structure.
 * This class should only be instantiated and used when the Android plugin is known to be present.
 */
internal class AndroidDirectoryHelper(
    private val project: Project,
    private val showErrorDialog: (errorMessage: String) -> Unit,
) {
    fun saveDrawableFile(
        drawableResourceFileInfo: DrawableResourceFileInfo,
    ) {
        val drawableDirectory: PsiDirectory = getDrawableDirectory() ?: return
        val drawableFile: PsiFile = drawableDirectory.createFile(drawableResourceFileInfo.name)
        try {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                val document = psiDocumentManager.getDocument(drawableFile)
                if (document != null) {
                    document.setText(drawableResourceFileInfo.content)
                    psiDocumentManager.commitDocument(document)
                }
                drawableFile.virtualFile?.let {
                    FileEditorManager.getInstance(project).openFile(it, true)
                }
            }
        } catch (
            exception: Exception,
        ) {
            showErrorDialog("Error downloading or saving file: ${exception.message}")
        }
    }

    private fun getDrawableDirectory(): PsiDirectory? {
        if (!isAndroidPluginInstalled()) {
            showErrorDialog("Android support plugin is not enabled!")
            return null
        }
        return try {
            val drawableDirectory = getPsiDrawableDirectory()
            if (drawableDirectory == null) {
                showErrorDialog("Could not find or create a drawable directory. Is this an Android project?")
                null
            } else {
                drawableDirectory
            }
        } catch (
            _: NoClassDefFoundError,
        ) {
            // This is a safeguard, the check above should prevent this.
            showErrorDialog("Android support is not available.")
            null
        }
    }

    private fun isAndroidPluginInstalled(): Boolean {
        val androidPluginId = PluginId.findId("org.jetbrains.android")
        return androidPluginId != null && PluginManagerCore.isPluginInstalled(
            id = androidPluginId,
        )
    }

    private fun getPsiDrawableDirectory(): PsiDirectory? {
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
        WriteCommandAction.runWriteCommandAction(project) {
            drawableDirectory = resourceDirectory.findSubdirectory("drawable")
                ?: resourceDirectory.createSubdirectory("drawable")
        }
        return drawableDirectory
    }
}
