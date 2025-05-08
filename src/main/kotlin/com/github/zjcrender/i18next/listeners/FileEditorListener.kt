package com.github.zjcrender.i18next.listeners

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

/**
 * Listener for file editor events.
 * Monitors file opening and selection changes to process translation calls.
 */
class FileEditorListener(private val project: Project) : FileEditorManagerListener {
  private val LOG = logger<FileEditorListener>()
  private val caretListeners = mutableMapOf<VirtualFile, CaretListener>()

  override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
    LOG.info("File opened: ${file.path}")

    // 判断是否是js或ts文件
    val psiFile = PsiManager.getInstance(project).findFile(file)
    if (!isJavaScriptOrTypeScriptFile(psiFile!!)) return

    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    if (editor !is Editor) return

    val listener = createCaretListener()
    editor.caretModel.addCaretListener(listener)
    caretListeners[file] = listener
  }

  override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
    caretListeners[file]?.let { listener ->
      val editor = source.getSelectedEditor(file)
      if (editor is Editor) {
        editor.caretModel.removeCaretListener(listener)
      }
    }
    caretListeners.remove(file)
  }

  private fun createCaretListener() = object : CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
      event.caret?.offset?.let { offset ->
        val editor = event.editor
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
          for (region in foldingModel.allFoldRegions) {
            if (region.group.toString().startsWith("i18next-translation")) {
              val startOffset = region.startOffset
              val endOffset = region.endOffset
              if (offset in startOffset..endOffset) {
                // 这里可以添加你想要的操作，比如更新翻译文本等
                LOG.info("Caret position changed in translation region: $startOffset to $endOffset")
                region.isExpanded = true
              } else {
                region.isExpanded = false
              }
            }

          }
        }
      }
    }
  }

  private fun isJavaScriptOrTypeScriptFile(file: PsiFile): Boolean {
    val language = file.language
    val displayName = language.displayName.lowercase()
    LOG.info("File language: ${language.id}, display name: $displayName")

    return language.isKindOf(JavascriptLanguage.INSTANCE) ||
      displayName.contains("typescript") ||
      displayName.contains("javascript")
  }
}
