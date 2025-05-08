package com.github.zjcrender.i18next.settings

import com.github.zjcrender.i18next.services.TranslationService
import com.github.zjcrender.i18next.util.LocalesScanner
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.dsl.builder.*
import java.io.File
import javax.swing.JComponent

/**
 * Configurable for I18next plugin settings.
 */
class I18nextConfigurable(private val project: Project) : Configurable {
  private val settings = project.service<I18nextSettings>()
  private val entryState = settings.state.copy()

  // configurations
  private var multilingualFolder: String = settings.state.multilingualFolder

  private var languages: List<String> = emptyList()
  private var previewLanguage: String = settings.state.previewLanguage
  private lateinit var previewLanguageComboBoxCell: Cell<ComboBox<String>>

  private var namespaces: List<String> = emptyList()
  private var defaultNamespace: String = settings.state.defaultNamespace
  private lateinit var defaultNamespaceComboBoxCell: Cell<ComboBox<String>>


  private var mainPanel: DialogPanel? = null

  override fun getDisplayName(): String = "I18next Settings"

  override fun createComponent(): JComponent {
    // Scan for languages and namespaces if locales directory is set
    if (multilingualFolder.isNotEmpty()) {
      refreshLanguages()
      refreshNamespaces()
    }

    return panel {
      row("Multilingual Folder:") {
        textField()
          .bindText(
            getter = {
              val base = project.basePath ?: ""
              if (multilingualFolder.startsWith(base)) {
                multilingualFolder.removePrefix(base).removePrefix(File.separator)
              } else {
                multilingualFolder
              }
            },
            setter = {
              multilingualFolder = it
            }
          )
          .columns(COLUMNS_MEDIUM)
          .enabled(false)

        button("Browse") {
          val directory = com.intellij.openapi.fileChooser.FileChooser.chooseFile(
            com.intellij.openapi.fileChooser.FileChooserDescriptor(false, true, false, false, false, false),
            project,
            null
          )
          if (directory != null) {
            multilingualFolder = directory.path
            refreshLanguages()
            updateLanguageComboBox()
          }
        }

        button("Detect") {
          val path = LocalesScanner.detectMultilingualFolder(project)
          if (path.isEmpty()) {
            Messages.showErrorDialog(project, "No locales directory detected", "Error")
            return@button
          }

          multilingualFolder = path
          refreshLanguages()
          updateLanguageComboBox()
        }
      }

      row("Preview Language:") {
        val modal = CollectionComboBoxModel(languages.ifEmpty { listOf("<No languages found>") })
        modal.selectedItem = previewLanguage
        previewLanguageComboBoxCell = comboBox(modal)
          .enabled(languages.isNotEmpty())
          .whenItemSelectedFromUi { selected ->
            previewLanguage = selected
            refreshNamespaces()
            updateNamespaceComboBox()
          }

      }

      row("Default Namespace:") {
        val modal = CollectionComboBoxModel(namespaces.ifEmpty { listOf("translation") })
        modal.selectedItem = defaultNamespace
        defaultNamespaceComboBoxCell = comboBox(modal)
          .enabled(true)
          .whenItemSelectedFromUi { selected ->
            defaultNamespace = selected
            updateUI()
          }
      }
    }.also { mainPanel = it }
  }

  private fun refreshLanguages() {
    val directory = File(multilingualFolder)
    if (!directory.exists() || !directory.isDirectory()) {
      Messages.showErrorDialog(project, "Invalid locales directory", "Error")
      return
    }

    languages = LocalesScanner.scanLanguages(directory)
  }

  private fun updateLanguageComboBox() {
    val comboBox = previewLanguageComboBoxCell.component
    val modal = CollectionComboBoxModel(languages.ifEmpty { listOf("<No languages found>") })

    if (previewLanguage !in languages) {
      previewLanguage = languages.first()
    }
    modal.selectedItem = previewLanguage

    comboBox.model = modal
    comboBox.isEnabled = languages.isNotEmpty()
    modal.update()
    comboBox.updateUI()

    refreshNamespaces()
    updateNamespaceComboBox()
  }

  private fun refreshNamespaces() {
    namespaces = LocalesScanner.scanNamespaces(File(multilingualFolder, previewLanguage))
  }

  private fun updateNamespaceComboBox() {
    val comboBox = defaultNamespaceComboBoxCell.component
    val modal = CollectionComboBoxModel(namespaces.ifEmpty { listOf("translation") })

    if (defaultNamespace !in namespaces) {
      defaultNamespace = namespaces.first()
    }
    modal.selectedItem = defaultNamespace

    comboBox.model = modal
    modal.update()
    comboBox.updateUI()
  }

  private fun updateUI() {
    mainPanel?.reset()
  }

  override fun isModified(): Boolean {
    return multilingualFolder != settings.state.multilingualFolder ||
      previewLanguage != settings.state.previewLanguage ||
      defaultNamespace != settings.state.defaultNamespace
  }

  override fun apply() {
    settings.apply(
      settings.state.copy(
        multilingualFolder = multilingualFolder,
        previewLanguage = previewLanguage,
        defaultNamespace = defaultNamespace
      )
    )
  }

  override fun reset() {
    multilingualFolder = settings.state.multilingualFolder
    previewLanguage = settings.state.previewLanguage
    defaultNamespace = settings.state.defaultNamespace

    refreshLanguages()

    mainPanel?.reset()
  }

  override fun disposeUIResources() {
    if (entryState == settings.state) return

    // restart translation service
    project.service<TranslationService>().setup()
  }

  companion object {
    private const val COLUMNS_MEDIUM = 30
  }
}
