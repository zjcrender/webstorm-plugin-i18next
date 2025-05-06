package com.github.zjcrender.i18next.listeners

import com.github.zjcrender.i18next.services.TranslationService
import com.github.zjcrender.i18next.settings.I18nextSettings
import com.intellij.json.JsonFileType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.annotations.NotNull


class JsonFileChangeListener(private val project: Project) : BulkFileListener {
  private val LOG = logger<JsonFileChangeListener>()
  private val TARGET_EXTENSIONS = setOf("js", "jsx", "ts", "tsx")

  override fun after(@NotNull events: List<@NotNull VFileEvent>) {
    val settings = project.service<I18nextSettings>()
    val translationService = project.service<TranslationService>()

    events
      .filterIsInstance<VFileContentChangeEvent>()
      .map { it.file }
      .filter { it.fileType is JsonFileType }
      .filter { it.parent.name == settings.state.selectedLanguage }
      .forEach { jsonFile ->
        val ns = jsonFile.nameWithoutExtension
        translationService.invoke("reload", listOf(ns))
        LOG.info("Reloaded translations for namespace '$ns' in language '${settings.state.selectedLanguage}'.")

        // 重新加载折叠区域
        val fileEditorManager = FileEditorManager.getInstance(project)
        val openFiles: Array<VirtualFile> = fileEditorManager.openFiles
        openFiles
          .filter { TARGET_EXTENSIONS.contains(it.extension) }
          .forEach { file ->
            val editor = fileEditorManager.getSelectedEditor(file) as? TextEditor
            editor?.let {
              val foldingModel = it.editor.foldingModel
              foldingModel.runBatchFoldingOperation {
                for (region in foldingModel.allFoldRegions) {
                  if (region.group.toString().startsWith("i18next-translation")) {
                    region.isExpanded = true
                    region.isExpanded = false
                  }

                }
              }
            }
          }
      }
  }
}