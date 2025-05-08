package com.github.zjcrender.i18next.services

import com.github.zjcrender.i18next.folding.TranslationFoldingReloader
import com.github.zjcrender.i18next.settings.I18nextSettings
import com.google.gson.Gson
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


data class TResponse(
  val to: Long,
  val data: Any,
)

/**
 * Service for managing the Deno translation server.
 * Ensures that only one server is running across all projects.
 */
@Suppress("UNCHECKED_CAST")
@Service(Service.Level.PROJECT)
class TranslationService(private val project: Project) : Disposable {
  private val LOG = logger<TranslationService>()
  private var process: Process? = null
  private var writer: OutputStreamWriter? = null
  private var reader: BufferedReader? = null
  private val responseQueue = LinkedBlockingQueue<TResponse>()

  private fun getResourceFilePath(
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

  fun invoke(action: String, args: List<*> = emptyList<Any>()): Long {
    if (process == null) return -1L

    val timestamp = System.currentTimeMillis()
    val command = mapOf("action" to action, "args" to args, "timestamp" to timestamp)

    val w: OutputStreamWriter = writer!!
    synchronized(w) {
      w.write(Gson().toJson(command) + "\n")
      w.flush()
    }

    return timestamp
  }

  fun <T> fetchResult(
    timestamp: Long,
    timeout: Long = 10L,
    condition: (TResponse) -> Boolean = { it.to == timestamp }
  ): T? {
    if (timestamp == -1L) return null

    val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout)

    while (true) {
      val remainingTime = deadline - System.nanoTime()
      if (remainingTime <= 0) {
        return null
      }

      val response = responseQueue.poll(remainingTime, TimeUnit.NANOSECONDS)
      if (response == null) {
        return null
      }

      if (condition(response)) {
        return response.data as? T
      }
    }
  }

  fun start(cwd: File = File(project.basePath!!)) {
    if (process != null) return

    val nodeJsInterpreter = NodeJsInterpreterManager.getInstance(project).interpreter
    val nodePath = if (nodeJsInterpreter is NodeJsLocalInterpreter) nodeJsInterpreter.interpreterSystemIndependentPath else "node"

    process = ProcessBuilder(nodePath, getResourceFilePath("index", ".js", "/i18next"))
      .directory(cwd)
      .redirectErrorStream(true)
      .start()

    writer = OutputStreamWriter(process!!.outputStream)
    reader = BufferedReader(InputStreamReader(process!!.inputStream))

    Thread {
      reader?.useLines { lines ->
        lines.forEach { line ->
          try {
            val stdout = Gson().fromJson(line, TResponse::class.java)
            responseQueue.add(stdout)
          } catch (e: Exception) {
            LOG.warn("Failed to parse response: $line", e)
          }
        }
      }
    }.start()
  }

  fun stop() {
    invoke("exit")
    process?.destroy()
    process = null
    writer = null
    reader = null
  }

  fun setup() {
    val state = project.service<I18nextSettings>().state

    stop()
    start()
    invoke(
      "setup",
      listOf<Any>(state.multilingualFolder, state.previewLanguage, state.defaultNamespace)
    )

    project.service<TranslationFoldingReloader>().scheduleReload()
  }

  fun translate(args: List<*>): String? {
    val r = fetchResult<String>(invoke("t", args))
    return r
  }

  override fun dispose() {
    stop()
    LOG.info("Service disposed")
  }

  companion object {
    fun getInstance(project: Project): TranslationService {
      return project.service<TranslationService>()
    }
  }
}
