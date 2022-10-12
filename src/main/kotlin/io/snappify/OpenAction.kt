package io.snappify

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.DataFlavor
import java.net.URLEncoder

class OpenAction : AnAction() {

    companion object {
        const val SNAPPIFY_URL = "https://snappify.io"
    }

    override fun actionPerformed(event: AnActionEvent) {
        PlatformDataKeys.COPY_PROVIDER.getData(event.dataContext)?.let {
            it.performCopy(event.dataContext)

            val psiFile = event.getData(CommonDataKeys.PSI_FILE)
            val fileName = psiFile?.virtualFile?.name ?: ""
            val extension = psiFile?.virtualFile?.extension
            val contents = CopyPasteManager
                    .getInstance()
                    .getContents<String>(DataFlavor.stringFlavor)
                    ?.trimIndent()

            openInSnappify(contents, encode(fileName), extension) {
                BrowserUtil.browse(it)
            }
        }
    }

    fun openInSnappify(contents: String?, fileName: String, extension: String?, browse: (url: String) -> Unit) {
        if (contents?.isNotEmpty() == true) {
            val url = buildUrl(
                    fileName,
                    language = LanguageMap.forFileExtension(extension),
                    code = encode(contents)
            )
            browse(url)
        }
    }

    private fun buildUrl(fileName: String, language: String, code: String): String {
        return "$SNAPPIFY_URL/new?p=1&f=$fileName&l=$language&c=$code"
    }

    private fun encode(s: String): String {
        return URLEncoder.encode(s, "UTF-8")
                .replace("+", "%20")
                .replace("%", "%25")
    }

    override fun update(event: AnActionEvent) {
        val context = event.dataContext
        val provider = PlatformDataKeys.COPY_PROVIDER.getData(context) ?: return
        val isCopyAvailable = provider.isCopyEnabled(context) && provider.isCopyVisible(context)
        event.presentation.apply {
            isVisible = isCopyAvailable
            isEnabled = isCopyAvailable
        }
    }
}