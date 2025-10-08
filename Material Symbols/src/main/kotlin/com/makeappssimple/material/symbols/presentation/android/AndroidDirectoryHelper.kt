package com.makeappssimple.material.symbols.presentation.android

import com.android.tools.idea.projectsystem.SourceProviderManager
import com.android.tools.idea.projectsystem.SourceProviders
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import com.makeappssimple.material.symbols.domain.model.DrawableResourceFileInfo
import org.jetbrains.android.facet.AndroidFacet

private val testModuleSuffices = setOf(".main", ".unitTest", ".androidTest", ".screenshotTest")

/**
 * A helper class to interact with Android project structure.
 * This class should only be instantiated and used when the Android plugin is known to be present.
 */
internal class AndroidDirectoryHelper(
    private val project: Project,
    private val showErrorDialog: (errorMessage: String) -> Unit,
) {
    fun getAndroidFacets(): List<AndroidFacet> {
        return project.modules.mapNotNull {
            AndroidFacet.getInstance(it)
        }.filter { facet ->
            testModuleSuffices.none(
                predicate = facet.module.name::endsWith,
            )
        }
    }

    fun saveDrawableFiles(
        drawableResourceFileInfoList: List<DrawableResourceFileInfo>,
        selectedModule: AndroidFacet,
    ) {
        val drawableDirectory: PsiDirectory = getDrawableDirectory(
            selectedModule = selectedModule,
        ) ?: return
        try {
            val createdFiles = mutableListOf<VirtualFile>()
            for (drawableResourceFileInfo in drawableResourceFileInfoList) {
                val drawableFile: PsiFile = try {
                    drawableDirectory.createFile(drawableResourceFileInfo.name)
                } catch (
                    incorrectOperationException: IncorrectOperationException,
                ) {
                    showErrorDialog("Error downloading or saving file: ${incorrectOperationException.message}")
                    null
                } ?: continue
                WriteCommandAction.runWriteCommandAction(project) {
                    val psiDocumentManager = PsiDocumentManager.getInstance(project)
                    val document = psiDocumentManager.getDocument(drawableFile)
                        ?: throw IllegalStateException("Unable to get document for file")
                    document.setText(drawableResourceFileInfo.content)
                    psiDocumentManager.commitDocument(document)
                    drawableFile.virtualFile?.let { virtualFile: VirtualFile ->
                        createdFiles.add(virtualFile)
                    }
                }
            }
            if (createdFiles.isNotEmpty()) {
                FileEditorManager.getInstance(project).openFile(createdFiles.first(), true)
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
