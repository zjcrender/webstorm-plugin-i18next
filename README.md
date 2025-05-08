# webstorm-plugin-i18next

![Build](https://github.com/zjcrender/webstorm-plugin-i18next/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/22407.svg)](https://plugins.jetbrains.com/plugin/22407)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22407.svg)](https://plugins.jetbrains.com/plugin/22407)

[中文文档](README-zh.md)

## Project Overview

webstorm-plugin-i18next is a WebStorm plugin that provides translation preview functionality for JavaScript and TypeScript projects using the i18next internationalization framework. The plugin automatically identifies translation keys in your code (such as `t("translation.key")`) and replaces them with the actual translated text, improving development efficiency and user experience.

## Key Features

- **Translation Preview**: Automatically folds i18next translation function calls (like `t("hello.world")`) into the actual translated text
- **Smart Cursor Handling**: Automatically expands folded regions when the cursor is on a translation key for easy editing
- **Multi-language Support**: Switch preview languages in project settings
- **Namespace Support**: Supports i18next namespaces, including automatic detection of namespaces from `const { t } = useTranslation("ns")` pattern
- **Auto-detection**: Automatically detects the locales directory in your project

## Technical Implementation

This plugin is developed based on the IntelliJ Platform Plugin Template and includes the following main components:

1. **TranslationFoldingBuilder**: Implements code folding functionality to replace translation keys with actual translated text. It identifies i18next translation function calls (like `t("key")`) and replaces them with the actual translated text. It also detects namespaces from patterns like `const { t } = useTranslation("namespace")`.

2. **FileEditorListener**: Listens for file editing events and handles folding/expanding logic when cursor position changes. This ensures that translation keys are automatically expanded when the cursor is on them, making it easy to edit the keys.

3. **TranslationService**: Manages communication with the Node.js translation server and handles translation requests. It starts a Node.js process, sends commands through standard input, and receives responses through standard output. This allows the plugin to leverage the existing i18next JavaScript libraries for accurate translation processing.

4. **I18nextSettings**: Stores and manages plugin configuration such as localization directory, selected language, and default namespace. These settings are persisted between IDE sessions and can be configured through the IDE settings panel.

5. **LocalesScanner**: Provides utility functions for automatically detecting the localization directory structure. It searches for a "locales" directory in the project, scans for available languages (as subdirectories), and identifies namespaces (as JSON files).

### Expected Directory Structure

The plugin expects a directory structure commonly used with i18next:

```
project/
└── locales/
    ├── en/
    │   ├── translation.json
    │   └── namespace2.json
    └── zh/
        ├── translation.json
        └── namespace2.json
```

The plugin will automatically detect this structure and load the appropriate translation files based on the selected language and namespace.

### Node.js Integration

The plugin uses a Node.js server to process translations, communicating with the server through standard input/output streams. This approach allows the plugin to leverage the existing i18next JavaScript libraries for accurate translation processing without reimplementing the i18next logic in Kotlin.

<!-- Plugin description -->
A plugin for i18next translation preview in JavaScript and TypeScript files.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "webstorm-plugin-i18next"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/22407) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/22407/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/zjcrender/webstorm-plugin-i18next/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

1. After installing the plugin, open a JavaScript or TypeScript file containing i18next translations
2. The plugin will automatically detect translation function calls and display translation previews
3. Configure the plugin through <kbd>Settings/Preferences</kbd> > <kbd>I18next Settings</kbd>

---

## Contributing

Contributions to this plugin are welcome! Here's how you can contribute:

1. **Report Issues**: If you find a bug or have a feature request, please create an issue on the [GitHub repository](https://github.com/zjcrender/webstorm-plugin-i18next/issues).

2. **Submit Pull Requests**: If you'd like to contribute code:
   - Fork the repository
   - Create a new branch for your feature or bugfix
   - Make your changes
   - Submit a pull request

3. **Development Setup**:
   - Clone the repository
   - Open the project in IntelliJ IDEA
   - Use Gradle tasks to build and test the plugin
   - Run the `runIde` task to test the plugin in a development instance of WebStorm

### Project Structure

- `src/main/kotlin`: Kotlin source code
- `src/main/resources`: Resource files, including the Node.js server code
- `build.gradle.kts`: Gradle build configuration

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
