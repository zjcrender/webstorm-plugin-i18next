package com.github.zjcrender.i18next.startup

import com.github.zjcrender.i18next.services.TranslationService
import com.github.zjcrender.i18next.settings.I18nextSettings
import com.github.zjcrender.i18next.util.I18nUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

/**
 * Activity that runs when a project is opened.
 * Starts the translation server and initializes the plugin.
 */
class I18nextActivity : ProjectActivity {
  private val LOG = logger<I18nextActivity>()

  override suspend fun execute(project: Project) {
    LOG.info("I18nextActivity started for project: ${project.name}")

    I18nUtil.setProject(project)

    val translationService = project.service<TranslationService>()
    val settings = project.service<I18nextSettings>().state

    translationService.start(File(project.basePath!!))
    translationService.invoke("setup", listOf<Any>(settings.localesDirectory, settings.selectedLanguage, settings.selectedNamespace))

  }

}

