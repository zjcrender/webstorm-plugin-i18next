package com.github.zjcrender.i18next.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.nio.file.Files

object I18nUtil {
  @Volatile
  private var currentProject: Project? = null

  fun setProject(project: Project) {
    currentProject = project
  }

  fun notify(
    content: String,
    title: String = "I18next Plugin",
    type: NotificationType = NotificationType.INFORMATION
  ) {
    NotificationGroupManager.getInstance()
      .getNotificationGroup("I18next Notifications")
      .createNotification(
        title,
        content,
        type
      )
      .notify(currentProject)
  }

  fun detectLocalePath(): String {
    val project = currentProject ?: return ""

    val basePath = project.basePath ?: return ""
    val excludeDirs = setOf("node_modules", ".git", "dist", "out", "build")

    fun scan(files: Array<java.io.File>, depth: Int = 1): String {
      if (depth > 5) return ""

      for (f in files) {
        if (f.name in excludeDirs) continue
        if (!f.isDirectory) continue

        if (f.name == "locales") {
          return f.absolutePath
        } else {
          val result = scan(f.listFiles() ?: emptyArray(), depth + 1)
          if (result.isNotEmpty()) {
            return result
          }
        }
      }
      return ""
    }

    return scan(java.io.File(basePath).listFiles())
  }

  fun getResourceFilePath(
    name: String,
    suffix: String,
    base: String = "/"
  ): String {
    val path = "$base/$name$suffix"
    val resourceStream = object {}.javaClass.getResourceAsStream(path)
      ?: throw IllegalArgumentException("Resource not found: $path")
    val tempFile = Files.createTempFile(name, suffix).toFile()
    tempFile.deleteOnExit() // JVM 退出时删除临时文件
    resourceStream.use { input ->
      tempFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    return tempFile.absolutePath
  }
}