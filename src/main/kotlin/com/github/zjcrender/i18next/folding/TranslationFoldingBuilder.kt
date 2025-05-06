package com.github.zjcrender.i18next.folding

import com.github.zjcrender.i18next.services.TranslationService
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.javascript.psi.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil

/**
 * Folding builder for i18next translation function calls.
 * Folds t("key") into the actual translation text.
 */
class TranslationFoldingBuilder : FoldingBuilderEx(), DumbAware {
  private val LOG = logger<TranslationFoldingBuilder>()

  override fun buildFoldRegions(
    node: PsiElement,
    document: Document,
    quick: Boolean
  ): Array<FoldingDescriptor> {
    val descriptors = mutableListOf<FoldingDescriptor>()
    val project = node.project

    // Skip if we're in a quick mode (e.g., during typing)
    if (quick) {
      return emptyArray()
    }

    try {
      node.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
          super.visitElement(element)

          if (element is JSCallExpression) {
            val method = element.methodExpression
            if (method is JSReferenceExpression && method.referenceName == "t") {
              processTranslationCall(element, descriptors, project)
            }
          }
        }
      })
    } catch (e: Exception) {
      LOG.error("Error building fold regions", e)
    }

    return descriptors.toTypedArray()
  }

  private fun findNamespace(tMethod: JSReferenceExpression): String? {
    val resolved = tMethod.reference?.resolve() as? JSVariable
    // 从这个变量声明节点，向上找解构赋值元素 { t } = useTranslation(...)
    // 注意 PSI 里解构其实会被映射成 JSDestructuringElement／JSDestructuringObject
    val destructuring = PsiTreeUtil.getParentOfType(resolved, JSDestructuringElement::class.java)
      ?: return null

    // 解构右侧的 initializer 就是 useTranslation(...) 这个调用
    val init = destructuring.initializer as? JSCallExpression ?: return null

    // 确保是我们要的函数名
    val initMethod = init.methodExpression as? JSReferenceExpression
    if (initMethod?.referenceName != "useTranslation") return null

    // 拿第一个参数，应该是一个 string literal
    val args = init.arguments
    if (args.isEmpty()) return null
    val nsArg = args[0] as? JSLiteralExpression
    return nsArg?.stringValue
  }

  private fun processTranslationCall(
    element: JSCallExpression,
    descriptors: MutableList<FoldingDescriptor>,
    project: Project
  ) {
    val argList = element.argumentList ?: return
    if (argList.arguments.isEmpty()) return

    // Get the first argument which should be the translation key
    val firstArg = argList.arguments.first()

    try {
      // Get the translation service instance
      val translationService = TranslationService.getInstance(project)

      // Find the namespace from const { t } = useTranslation("ns") if available
      val namespace: String? = findNamespace(element.methodExpression as JSReferenceExpression)

      // Get the arguments for the translation
      val args = collectArguments(element).toMutableList()

      // Add namespace information if found
      if (namespace != null) {
        // Add namespace as the last argument if it's not already included
        if (args.size == 1 && args[0] is String) {
          // If we only have the key, add the namespace as a second argument
          args.add(mapOf("ns" to namespace))
        } else if (args.size >= 2 && args[1] is Map<*, *>) {
          // If we have options as the second argument, add/keep the ns property
          @Suppress("UNCHECKED_CAST")
          val options = args[1] as MutableMap<String, Any?>
          if (options["ns"] == null) {
            options["ns"] = namespace
          }
        }
      }

      // Get the translation for the key with namespace
      val translation = translationService.translate(args)

      if (translation.isNullOrEmpty()) return

      // Create a unique folding group for this translation
      val group = FoldingGroup.newGroup("i18next-translation-${args[0]}")

      // Create the folding descriptor
      val startOffset = firstArg.textRange.startOffset
      val endOffset = argList.arguments.last().textRange.endOffset

      descriptors.add(
        FoldingDescriptor(
          argList.node,
          TextRange(startOffset, endOffset),
          group,
          translation
        )
      )
    } catch (e: Exception) {
      LOG.error("Error processing translation call", e)
    }
  }

  private fun collectArguments(callExpression: JSCallExpression): List<Any?> {
    val arguments = mutableListOf<Any?>()

    // 遍历函数调用的参数
    callExpression.arguments.forEach { argument ->
      when (argument) {
        is JSLiteralExpression -> {
          // 字面量值
          arguments.add(argument.value)
        }

        is JSObjectLiteralExpression -> {
          // 对象字面量，收集其属性
          arguments.add(argument.properties.associate { it.name to (it.value as? JSLiteralExpression)?.value })
        }

        else -> {
          // 其他类型的参数，直接记录其文本内容
          arguments.add(argument.text)
        }
      }
    }

    return arguments
  }

  override fun getPlaceholderText(node: ASTNode): String {
    // This is a fallback if the translation couldn't be determined
    return "..."
  }

  override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}
