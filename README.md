# webstorm-plugin-i18next

![Build](https://github.com/zjcrender/webstorm-plugin-i18next/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

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

1. **TranslationFoldingBuilder**: Implements code folding functionality to replace translation keys with actual translated text
2. **FileEditorListener**: Listens for file editing events and handles folding/expanding logic when cursor position changes
3. **TranslationService**: Manages communication with the Node.js translation server and handles translation requests
4. **I18nextSettings**: Stores and manages plugin configuration such as localization directory, selected language, and namespace
5. **I18nUtil**: Provides utility functions like automatic detection of localization directories and displaying notifications

The plugin uses a Node.js server to process translations, communicating with the server through standard input/output streams and leveraging existing i18next JavaScript libraries for translation processing.

<!-- Plugin description -->
A plugin for i18next translation preview in JavaScript and TypeScript files.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "webstorm-plugin-i18next"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/zjcrender/webstorm-plugin-i18next/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

1. After installing the plugin, open a JavaScript or TypeScript file containing i18next translations
2. The plugin will automatically detect translation function calls and display translation previews
3. Configure the plugin through <kbd>Settings/Preferences</kbd> > <kbd>I18next Settings</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
