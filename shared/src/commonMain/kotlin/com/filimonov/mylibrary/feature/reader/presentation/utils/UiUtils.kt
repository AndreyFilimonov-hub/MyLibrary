package com.filimonov.mylibrary.feature.reader.presentation.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

fun htmlToAnnotatedString(html: String): AnnotatedString {
    val document = Ksoup.parse(html)

    val blockTags = setOf("p", "div", "section", "blockquote", "li")
    val headerTags = setOf("h1", "h2", "h3", "h4", "h5", "h6")

    return buildAnnotatedString {
        fun visit(node: Node) {
            when (node) {
                is TextNode -> {
                    val text = node.text()
                    if (text.isNotBlank()) append(text)
                }
                is Element -> {
                    val tag = node.tagName().lowercase()

                    if (tag == "a" && node.attr("href").isBlank()) return

                    if (tag in setOf("script", "style", "head", "title", "meta", "link")) return

                    val start = length

                    if (tag in blockTags || tag in headerTags) {
                        if (length > 0) append("\n\n")
                    } else if (tag == "br") {
                        append("\n")
                    }

                    node.childNodes.forEach { visit(it) }

                    when(tag) {
                        "b", "strong" -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                        "i", "em", "cite" -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                        "u" -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, length)
                        "s", "strike", "del" -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, length)
                        "sub" -> addStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 12.sp), start, length)
                        "sup" -> addStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 12.sp), start, length)
                    }

                    if (tag in headerTags) {
                        val size = when (tag) {
                            "h1" -> 26.sp
                            "h2" -> 22.sp
                            "h3" -> 19.sp
                            else -> 17.sp
                        }
                        addStyle(SpanStyle(fontSize = size, fontWeight = FontWeight.Bold), start, length)
                        addStyle(ParagraphStyle(textAlign = TextAlign.Center), start, length)
                    }

                    val inlineStyle = node.attr("style")
                    if (inlineStyle.isNotBlank()) {
                        if (Regex("font-weight\\s*:\\s*bold").containsMatchIn(inlineStyle)) {
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                        }
                        if (Regex("font-weight\\s*:\\s*italic").containsMatchIn(inlineStyle)) {
                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                        }
                    }
                }
            }
        }
        document.body().childNodes.forEach { visit(it) }
    }
        .let { raw ->
        val trimmedText = raw.text.trim()
        val startOffSet = raw.text.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
        if (trimmedText.isEmpty()) return@let AnnotatedString("")
        raw.subSequence(startOffSet, startOffSet + trimmedText.length)
    }
}
