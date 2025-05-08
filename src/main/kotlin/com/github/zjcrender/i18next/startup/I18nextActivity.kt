package com.github.zjcrender.i18next.startup

import com.github.zjcrender.i18next.services.TranslationService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity


class I18nextActivity : ProjectActivity {
  private val LOG = logger<I18nextActivity>()

  override suspend fun execute(project: Project) {
    LOG.info("I18nextActivity started for project: ${project.name}")
    project.service<TranslationService>().setup()
  }

}