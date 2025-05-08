# I18next Preview

![Build](https://github.com/zjcrender/webstorm-plugin-i18next/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/22407.svg)](https://plugins.jetbrains.com/plugin/22407)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22407.svg)](https://plugins.jetbrains.com/plugin/22407)

## 项目概述

I18next Preview 是一个 WebStorm 插件，为使用 i18next 国际化框架的 JavaScript 和 TypeScript 项目提供翻译预览功能。该插件能够自动识别代码中的翻译键（如 `t("translation.key")`），并将其替换为实际的翻译文本，从而提高开发效率和用户体验。

## 主要功能

- **翻译预览**：自动将 i18next 翻译函数调用（如 `t("hello.world")`）折叠为实际的翻译文本
- **智能光标处理**：当光标位于翻译键上时自动展开折叠区域，便于编辑
- **多语言支持**：支持在项目设置中切换预览语言
- **命名空间支持**：支持 i18next 的命名空间功能，包括自动检测 `const { t } = useTranslation("ns")` 模式中的命名空间
- **自动检测**：能够自动检测项目中的 locales 目录

## 技术实现

该插件基于 IntelliJ Platform Plugin Template 开发，主要包含以下组件：

1. **TranslationFoldingBuilder**：实现代码折叠功能，将翻译键替换为实际翻译文本。它能识别 i18next 翻译函数调用（如 `t("key")`），并将其替换为实际的翻译文本。它还能从 `const { t } = useTranslation("namespace")` 这样的模式中检测命名空间。

2. **FileEditorListener**：监听文件编辑事件，处理光标位置变化时的折叠/展开逻辑。这确保了当光标位于翻译键上时，折叠区域会自动展开，便于编辑。

3. **TranslationService**：管理与 Node.js 翻译服务器的通信，处理翻译请求。它启动一个 Node.js 进程，通过标准输入发送命令，通过标准输出接收响应。这使得插件能够利用现有的 i18next JavaScript 库进行准确的翻译处理。

4. **I18nextSettings**：存储和管理插件配置，如本地化目录、选定语言和默认命名空间。这些设置在 IDE 会话之间持久保存，可以通过 IDE 设置面板进行配置。

5. **LocalesScanner**：提供用于自动检测本地化目录结构的工具函数。它会在项目中搜索 "locales" 目录，扫描可用的语言（作为子目录），并识别命名空间（作为 JSON 文件）。

### 预期目录结构

插件期望使用 i18next 常用的目录结构：

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

插件将自动检测此结构，并根据选定的语言和命名空间加载适当的翻译文件。

### Node.js 集成

插件使用 Node.js 服务器处理翻译，通过标准输入/输出流与服务器通信。这种方法使插件能够利用现有的 i18next JavaScript 库进行准确的翻译处理，而无需在 Kotlin 中重新实现 i18next 逻辑。

<!-- Plugin description -->
一个用于在 JavaScript 和 TypeScript 文件中预览 i18next 翻译的插件。
<!-- Plugin description end -->

## 示例
![Demo](./media/demo.gif)

## 安装方法

- **通过 IDE 内置插件系统**：

  <kbd>设置/首选项</kbd> > <kbd>插件</kbd> > <kbd>市场</kbd> > <kbd>搜索 "I18next Preview"</kbd> >
  <kbd>安装</kbd>

- **通过 JetBrains 插件市场**：

  访问 [JetBrains 插件市场](https://plugins.jetbrains.com/plugin/22407) 并点击 <kbd>安装到...</kbd> 按钮。

  您也可以从 [最新发布版本](https://plugins.jetbrains.com/plugin/22407/versions) 下载并手动安装：
  <kbd>设置/首选项</kbd> > <kbd>插件</kbd> > <kbd>⚙️</kbd> > <kbd>从磁盘安装插件...</kbd>

- **手动安装**：

  下载 [最新发布版本](https://github.com/zjcrender/webstorm-plugin-i18next/releases/latest) 并手动安装：
  <kbd>设置/首选项</kbd> > <kbd>插件</kbd> > <kbd>⚙️</kbd> > <kbd>从磁盘安装插件...</kbd>

## 使用方法

1. 安装插件后，打开包含 i18next 翻译的 JavaScript 或 TypeScript 文件
2. 插件将自动检测翻译函数调用并显示翻译预览
3. 通过 <kbd>设置/首选项</kbd> > <kbd>I18next Settings</kbd> 配置插件

---

## 贡献指南

欢迎为此插件做出贡献！以下是贡献的方式：

1. **报告问题**：如果您发现了 bug 或有功能请求，请在 [GitHub 仓库](https://github.com/zjcrender/webstorm-plugin-i18next/issues) 创建一个 issue。

2. **提交 Pull Request**：如果您想贡献代码：
   - Fork 仓库
   - 为您的功能或 bug 修复创建一个新分支
   - 进行更改
   - 提交 Pull Request

3. **开发环境设置**：
   - 克隆仓库
   - 在 IntelliJ IDEA 中打开项目
   - 使用 Gradle 任务构建和测试插件
   - 运行 `runIde` 任务在 WebStorm 的开发实例中测试插件

### 项目结构

- `src/main/kotlin`：Kotlin 源代码
- `src/main/resources`：资源文件，包括 Node.js 服务器代码
- `build.gradle.kts`：Gradle 构建配置

---
插件基于 [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template) 开发。
