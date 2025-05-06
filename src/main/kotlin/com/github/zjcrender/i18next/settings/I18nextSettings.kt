package com.github.zjcrender.i18next.settings

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
        var localesDirectory: String = "",
        var selectedLanguage: String = "",
        var selectedNamespace: String = "translation"
    )
    
    private var myState = State()
    
    override fun getState(): State = myState
    
    override fun loadState(state: State) {
        myState = state
    }
    
    /**
     * Gets the locales directory path.
     */
    fun getLocalesDirectory(): String = myState.localesDirectory
    
    /**
     * Sets the locales directory path.
     */
    fun setLocalesDirectory(directory: String) {
        myState.localesDirectory = directory
    }
    
    /**
     * Gets the selected language.
     */
    fun getSelectedLanguage(): String = myState.selectedLanguage
    
    /**
     * Sets the selected language.
     */
    fun setSelectedLanguage(language: String) {
        myState.selectedLanguage = language
    }
    
    /**
     * Gets the selected namespace.
     */
    fun getSelectedNamespace(): String = myState.selectedNamespace
    
    /**
     * Sets the selected namespace.
     */
    fun setSelectedNamespace(namespace: String) {
        myState.selectedNamespace = namespace
    }
    
    companion object {
        fun getInstance(project: Project): I18nextSettings = project.service<I18nextSettings>()
    }
}