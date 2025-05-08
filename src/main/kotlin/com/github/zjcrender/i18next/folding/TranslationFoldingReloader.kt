package com.github.zjcrender.i18next.folding

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm

@Service(Service.Level.PROJECT)
class TranslationFoldingReloader(
  private val project: Project,
) : Disposable {

  // 在 Swing/EDT 上执行定时任务
  private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

  /**
   * 外部调用此方法来“防抖”地重建折叠区域。
   * 多次调用，只会在 delayMs 后执行最后一次的 reloadNow().
   */
  fun scheduleReload(delayMs: Long = 100) {
    alarm.cancelAllRequests()
    alarm.addRequest({ reloadNow() }, delayMs)
  }

  /**
   * 真正做遍历 openFiles 并重建折叠的逻辑
   */
  private fun reloadNow() {
    // 确保文档/PSI 同步已经完成
    ApplicationManager.getApplication().invokeLater {
      val fileEditorManager = FileEditorManager.getInstance(project)
      val openFiles: Array<VirtualFile> = fileEditorManager.openFiles
      val targetExts = setOf("js", "jsx", "ts", "tsx")

      openFiles
        .filter { it.extension in targetExts }
        .forEach { vFile ->
          val editor = fileEditorManager.getSelectedEditor(vFile) as? TextEditor
          editor?.let {
            val foldingModel = it.editor.foldingModel
            foldingModel.runBatchFoldingOperation {
              for (region in foldingModel.allFoldRegions) {
                if (region.group.toString().startsWith("i18next-translation")) {
                  region.isExpanded = true
                  region.isExpanded = false
                }
              }
            }
          }
        }
    }
  }

  override fun dispose() {
    // Alarm 会在 parentDisposable 被 dispose 时自动清理
  }

  companion object {
    fun getInstance(project: Project): TranslationFoldingReloader = project.service<TranslationFoldingReloader>()
  }
}