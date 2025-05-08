package com.github.zjcrender.i18next.listeners

import com.github.zjcrender.i18next.folding.TranslationFoldingReloader
import com.github.zjcrender.i18next.services.TranslationService
import com.github.zjcrender.i18next.settings.I18nextSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.annotations.NotNull


class JsonFileChangeListener(private val project: Project) : BulkFileListener {
  private val LOG = logger<JsonFileChangeListener>()

  override fun after(@NotNull events: List<@NotNull VFileEvent>) {
    val settings = project.service<I18nextSettings>()
    val translationService = project.service<TranslationService>()

    events
      .filterIsInstance<VFileContentChangeEvent>()
      .map { it.file }
      .filter { it.extension == "json" }
      .filter { it.parent.name == settings.state.previewLanguage }
      .forEach { jsonFile ->
        val ns = jsonFile.nameWithoutExtension
        translationService.invoke("reload", listOf(ns))
        LOG.info("Reloaded translations for namespace '$ns' in language '${settings.state.previewLanguage}'.")

        // 重新加载折叠区域
        project.service<TranslationFoldingReloader>().scheduleReload()
      }
  }
}