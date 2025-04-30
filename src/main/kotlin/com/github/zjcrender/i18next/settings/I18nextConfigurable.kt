package com.github.zjcrender.i18next.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.*
import java.io.File
import javax.swing.JComponent
import com.github.zjcrender.i18next.util.I18nUtil
import com.intellij.openapi.diagnostic.thisLogger

/**
 * Configurable for I18next plugin settings.
 */
class I18nextConfigurable(private val project: Project) : Configurable {
  private val settings = I18nextSettings.getInstance(project)
  private var localesDirectory: String = settings.getLocalesDirectory()
  private var selectedLanguage: String = settings.getSelectedLanguage()
  private var selectedNamespace: String = settings.getSelectedNamespace()

  private var languages: List<String> = emptyList()
  private var namespaces: List<String> = emptyList()

  private var mainPanel: DialogPanel? = null

  override fun getDisplayName(): String = "I18next Settings"

  override fun createComponent(): JComponent {
    // Scan for languages and namespaces if locales directory is set
    if (localesDirectory.isNotEmpty()) {
      scanLocalesDirectory()
    }

    return panel {
      row("Locales Directory:") {
        textField()
          .bindText(
            getter = {
              val base = project.basePath ?: ""
              if (localesDirectory.startsWith(base)) {
                localesDirectory.removePrefix(base).removePrefix(File.separator)
              } else {
                localesDirectory
              }
            },
            setter = {
              localesDirectory = it
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
            localesDirectory = directory.path
            thisLogger().info("Selected locales directory: $localesDirectory")
            scanLocalesDirectory()
          }
        }

        button("Detect") {
          val path = I18nUtil.detectLocalePath()
          if (path.isEmpty()) {
            Messages.showErrorDialog(project, "No locales directory detected", "Error")
            return@button
          }

          localesDirectory = path
          scanLocalesDirectory()
        }
      }

      row("Language:") {
        val model = com.intellij.ui.CollectionComboBoxModel(languages.ifEmpty { listOf("<No languages found>") })
        if (languages.isNotEmpty() && languages.contains(selectedLanguage)) {
          model.selectedItem = selectedLanguage
        } else if (languages.isNotEmpty()) {
          model.selectedItem = languages.first()
          selectedLanguage = languages.first()
        }

        comboBox(model)
          .enabled(languages.isNotEmpty())
          .whenItemSelectedFromUi { selected ->
            selectedLanguage = selected as String
          }
      }

      row("Namespace:") {
        val defaultNamespaces = namespaces.ifEmpty { listOf("translation") }
        val model = com.intellij.ui.CollectionComboBoxModel(defaultNamespaces)

        if (namespaces.isNotEmpty() && namespaces.contains(selectedNamespace)) {
          model.selectedItem = selectedNamespace
        } else if (namespaces.isNotEmpty()) {
          val defaultNamespace = if (namespaces.contains("translation")) "translation" else namespaces.first()
          model.selectedItem = defaultNamespace
          selectedNamespace = defaultNamespace
        } else {
          model.selectedItem = "translation"
          selectedNamespace = "translation"
        }

        comboBox(model)
          .enabled(namespaces.isNotEmpty())
          .whenItemSelectedFromUi { selected ->
            selectedNamespace = selected as String
          }
      }
    }.also { mainPanel = it }
  }

  private fun scanLocalesDirectory() {
    val directory = File(localesDirectory)
    if (!directory.exists() || !directory.isDirectory) {
      Messages.showErrorDialog(project, "Invalid locales directory", "Error")
      return
    }

    // Scan for languages (subdirectories)
    val languageDirs = directory.listFiles { file -> file.isDirectory }
    languages = languageDirs?.map { it.name } ?: emptyList()

    // If no language is selected or the selected language is not in the list, select the first one
    if (selectedLanguage.isEmpty() || !languages.contains(selectedLanguage)) {
      selectedLanguage = languages.firstOrNull() ?: ""
    }

    if (selectedLanguage.isEmpty()) {
      namespaces = listOf("translation", "OK")
      selectedNamespace = "translation"
      return
    } else {
      // Scan for namespaces (JSON files in the selected language directory)
      val namespaceFiles = File(directory, selectedLanguage).listFiles { file ->
        file.isFile && file.extension.equals("json", ignoreCase = true)
      }
      namespaces = namespaceFiles?.map { it.nameWithoutExtension } ?: listOf("translation")

      // If no namespace is selected or the selected namespace is not in the list, select "translation" or the first one
      if (selectedNamespace.isEmpty() || !namespaces.contains(selectedNamespace)) {
        selectedNamespace = namespaces.first()
      }
    }

    updateUI()

  }

  private fun updateUI() {
    mainPanel?.reset()
  }

  override fun isModified(): Boolean {
    return localesDirectory != settings.getLocalesDirectory() ||
      selectedLanguage != settings.getSelectedLanguage() ||
      selectedNamespace != settings.getSelectedNamespace()
  }

  override fun apply() {
    settings.setLocalesDirectory(localesDirectory)
    settings.setSelectedLanguage(selectedLanguage)
    settings.setSelectedNamespace(selectedNamespace)
  }

  override fun reset() {
    localesDirectory = settings.getLocalesDirectory()
    selectedLanguage = settings.getSelectedLanguage()
    selectedNamespace = settings.getSelectedNamespace()

    if (localesDirectory.isNotEmpty()) {
      scanLocalesDirectory()
    }

    mainPanel?.reset()
  }

  companion object {
    private const val COLUMNS_MEDIUM = 30
  }
}
