package com.makeappssimple.material.symbols.android

import com.android.tools.idea.projectsystem.SourceProviderManager
import com.android.tools.idea.projectsystem.SourceProviders
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
    fun getAndroidFacets(): List<AndroidFacet> {
        val androidFacets: List<AndroidFacet> = project.modules.mapNotNull {
            AndroidFacet.getInstance(it)
        }.filter {
            !it.module.name.endsWith(".main") &&
                    !it.module.name.endsWith(".unitTest") &&
                    !it.module.name.endsWith(".androidTest")
        }
        return androidFacets
    }

    fun saveDrawableFile(
        drawableResourceFileInfo: DrawableResourceFileInfo,
        selectedModule: AndroidFacet,
    ) {
        val drawableDirectory: PsiDirectory = getDrawableDirectory(
            selectedModule = selectedModule,
        ) ?: return
        val drawableFile: PsiFile = drawableDirectory.createFile(drawableResourceFileInfo.name)
        try {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                val document = psiDocumentManager.getDocument(drawableFile)
                    ?: throw IllegalStateException("Unable to get document for file")
                document.setText(drawableResourceFileInfo.content)
                psiDocumentManager.commitDocument(document)
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

    private fun getDrawableDirectory(
        selectedModule: AndroidFacet,
    ): PsiDirectory? {
        if (!isAndroidPluginInstalled()) {
            showErrorDialog("Android support plugin is not enabled!")
            return null
        }
        return try {
            val drawableDirectory = getPsiDrawableDirectory(
                selectedModule = selectedModule,
            )
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

    private fun getPsiDrawableDirectory(
        selectedModule: AndroidFacet,
    ): PsiDirectory? {
        val sourceProvidersManager: SourceProviders = SourceProviderManager.Companion.getInstance(
            facet = selectedModule,
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
