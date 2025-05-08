package com.github.zjcrender.i18next.util

import com.intellij.openapi.project.Project
import java.io.File

object LocalesScanner {

  fun detectMultilingualFolder(project: Project): String {
    val basePath = project.basePath ?: return ""
    val excludeDirs = setOf("node_modules", ".git", "dist", "out", "build")

    fun scan(files: Array<File>, depth: Int = 1): String {
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

    return scan(File(basePath).listFiles())
  }

  fun scanLanguages(root: File): List<String> {
    val languageDirs = root.listFiles { file -> file.isDirectory }
    return languageDirs?.map { it.name } ?: emptyList()
  }

  fun scanNamespaces(root: File): List<String> {
    val namespaceFiles = root.listFiles { file ->
      file.isFile && file.extension.equals("json", ignoreCase = true)
    }
    return namespaceFiles?.map { it.nameWithoutExtension } ?: emptyList()
  }
}