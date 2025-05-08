package com.github.zjcrender.i18next.settings

import com.github.zjcrender.i18next.util.LocalesScanner
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Persistent settings for the I18next plugin.
 * Stores the locales directory, selected language, and namespace.
 */
@Service(Service.Level.PROJECT)
@State(
  name = "I18nextSettings",
  storages = [Storage("i18nextSettings.xml")]
)
class I18nextSettings(private val project: Project) : PersistentStateComponent<I18nextSettings.State> {

  /**
   * The state class that holds the actual settings values.
   */
  data class State(
    var multilingualFolder: String = "",
    var previewLanguage: String = "",
    var defaultNamespace: String = "translation"
  )

  @Volatile
  private var myState = State()

  override fun getState(): State = myState

  override fun loadState(state: State) {
    myState = verify(state)
  }

  fun verify(state: State): State {
    var multilingualFolder: String = state.multilingualFolder
    if (multilingualFolder.isEmpty()) {
      multilingualFolder = LocalesScanner.detectMultilingualFolder(project)
    }

    if (multilingualFolder.isEmpty()) {
      NotificationGroupManager.getInstance()
        .getNotificationGroup("I18next Notifications")
        .createNotification(
          "I18next initialize failed",
          "Locales directory not found",
          NotificationType.WARNING
        )
        .notify(project)
    }

    return state.copy(multilingualFolder = multilingualFolder)
  }

  fun apply(state: State) {
    myState = state
    project.save()
  }

  companion object {
    fun getInstance(project: Project): I18nextSettings = project.service<I18nextSettings>()
  }
}