package com.github.zjcrender.i18next.folding

import com.github.zjcrender.i18next.services.TranslationService
import com.github.zjcrender.i18next.util.I18nUtil
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
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
      val translationService = TranslationService.getInstance()

      // Get the translation for the key
      val translation = translationService.translate(
        collectArguments(element)
      )

      if (translation.isNullOrEmpty()) return

      // Create a unique folding group for this translation
      val group = FoldingGroup.newGroup("i18next-translation-${element.textRange.startOffset}")

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

  private fun collectFunctionProperties(callExpression: JSCallExpression): Map<String, Any> {
    val properties = mutableMapOf<String, String>()

    val functionElement =
      ReferencesSearch.search(callExpression, GlobalSearchScope.projectScope(callExpression.project))
        .findFirst()?.element
        ?: return properties

    val containingFile = callExpression.containingFile
    PsiTreeUtil.collectElements(containingFile) { element ->
      if (element is JSAssignmentExpression) {
        val lOperand = element.lOperand
        val rOperand = element.rOperand

        // Check if the left operand starts with "t."
        if (lOperand is JSReferenceExpression && lOperand.qualifier?.text == "t") {
          val propertyName = lOperand.referenceName ?: return@collectElements false
          val propertyValue = rOperand?.text ?: "undefined"

          // Add to the properties map
          properties[propertyName] = propertyValue
        }
      }
      true
    }

    return properties
  }

  override fun getPlaceholderText(node: ASTNode): String {
    // This is a fallback if the translation couldn't be determined
    return "..."
  }

  override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}
