package com.filimonov.mylibrary.feature.reader.presentation.reader

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.parser.Parser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class LazyBookPaginator(
    private val chapters: List<Chapter>,
    val style: TextStyle,
    val containerSize: IntSize,
    private val textMeasurer: TextMeasurer
) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val chapterPages = mutableStateMapOf<Int, List<AnnotatedString>>()
    private val inProgress = mutableMapOf<Int, Deferred<List<AnnotatedString>>>()

    private val pageCounts = mutableStateMapOf<Int, Int>()
    var isFullyCounted = MutableStateFlow(false)
        private set

    suspend fun countAllPagesInBackground() = coroutineScope {
        val semaphore = Semaphore(4)
        val jobs = chapters.indices.map { chapterIndex ->
            async(Dispatchers.Default) {
                semaphore.withPermit {
                    val count = chapterPages[chapterIndex]?.size ?: run {
                        val annotated = htmlToAnnotatedString(chapters[chapterIndex].content)
                        val pages =
                            paginateChapterGreedy(annotated, textMeasurer, style, containerSize)
                        chapterPages[chapterIndex] = pages
                        pages.size
                    }
                    pageCounts[chapterIndex] = count
                }
            }
        }
        jobs.awaitAll()
        isFullyCounted.value = true
    }

    fun pagesFor(chapterIndex: Int): List<AnnotatedString>? = chapterPages[chapterIndex]

    fun ensurePaginated(chapterIndex: Int) {
        if (chapterIndex !in chapters.indices || chapterIndex in chapterPages) return
        scope.launch { ensurePaginatedAwait(chapterIndex) }
    }

    suspend fun ensurePaginatedAwait(chapterIndex: Int): List<AnnotatedString> {
        return withContext(Dispatchers.Default) {
            chapterPages[chapterIndex]?.let { return@withContext it }
            inProgress[chapterIndex]?.let { return@withContext it.await() }

            val deferred = scope.async(Dispatchers.Default) {
                val annotated = htmlToAnnotatedString(chapters[chapterIndex].content)
                paginateChapterGreedy(annotated, textMeasurer, style, containerSize)
            }
            inProgress[chapterIndex] = deferred
            val result = deferred.await()
            chapterPages[chapterIndex] = result
            pageCounts[chapterIndex] = result.size
            inProgress.remove(chapterIndex)
            result
        }
    }

    fun globalPageIndex(chapterIndex: Int, pageInChapter: Int): Int? {
        var acc = 0
        for (i in 0 until chapterIndex) {
            acc += pageCounts[i] ?: return null
        }

        return acc + pageInChapter
    }

    fun totalPages(): Int? {
        if (!isFullyCounted.value) return null
        return pageCounts.values.sum().coerceAtLeast(1)
    }

    fun searchByQuery(query: String): List<SearchResult> {
        if (!isFullyCounted.value || query.isBlank()) return emptyList()

        val result = mutableListOf<SearchResult>()

        for (chapterIndex in chapters.indices) {
            val pages = chapterPages[chapterIndex] ?: continue

            pages.forEachIndexed { pageIndex, page ->
                val text = page.text
                var from = 0
                while (true) {
                    val matchIndex = text.indexOf(query, from, true)
                    if (matchIndex == -1) break

                    val snippetStart = (matchIndex - 30).coerceIn(0, text.length)
                    val snippetEnd = (matchIndex + query.length + 30).coerceIn(snippetStart, text.length)
                    val snippet = "...${text.substring(snippetStart, snippetEnd)}..."

                    result.add(
                        SearchResult(
                            chapterIndex,
                            pageIndex,
                            globalPageIndex(chapterIndex, pageIndex) ?: 0,
                            snippet
                        )
                    )
                    from = matchIndex + query.length
                }
            }
        }

        return result
    }

    fun resolveGlobalPage(globalPageIndex: Int): Pair<Int, Int>? {
        if (!isFullyCounted.value) return null

        var acc = 0
        for (chapterIndex in chapters.indices) {
            val count = pageCounts[chapterIndex] ?: return null
            if (globalPageIndex < acc + count) {
                return chapterIndex to (globalPageIndex - acc)
            }
            acc += count
        }
        return chapters.lastIndex to 0
    }

    fun htmlToAnnotatedString(html: String): AnnotatedString {
        val document = Ksoup.parse(html, Parser.xmlParser())

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

                        when (tag) {
                            "b", "strong" -> addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                start,
                                length
                            )

                            "i", "em", "cite" -> addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                start,
                                length
                            )

                            "u" -> addStyle(
                                SpanStyle(textDecoration = TextDecoration.Underline),
                                start,
                                length
                            )

                            "s", "strike", "del" -> addStyle(
                                SpanStyle(textDecoration = TextDecoration.LineThrough),
                                start,
                                length
                            )

                            "sub" -> addStyle(
                                SpanStyle(
                                    baselineShift = BaselineShift.Subscript,
                                    fontSize = 12.sp
                                ), start, length
                            )

                            "sup" -> addStyle(
                                SpanStyle(
                                    baselineShift = BaselineShift.Superscript,
                                    fontSize = 12.sp
                                ), start, length
                            )
                        }

                        if (tag in headerTags) {
                            val size = when (tag) {
                                "h1" -> 26.sp
                                "h2" -> 22.sp
                                "h3" -> 19.sp
                                else -> 17.sp
                            }
                            addStyle(
                                SpanStyle(
                                    fontSize = size,
                                    fontWeight = FontWeight.Bold
                                ), start, length
                            )
                            addStyle(
                                ParagraphStyle(textAlign = TextAlign.Center),
                                start,
                                length
                            )
                        }

                        val inlineStyle = node.attr("style")
                        if (inlineStyle.isNotBlank()) {
                            if (Regex("font-weight\\s*:\\s*bold").containsMatchIn(inlineStyle)) {
                                addStyle(
                                    SpanStyle(fontWeight = FontWeight.Bold),
                                    start,
                                    length
                                )
                            }
                            if (Regex("font-weight\\s*:\\s*italic").containsMatchIn(inlineStyle)) {
                                addStyle(
                                    SpanStyle(fontStyle = FontStyle.Italic),
                                    start,
                                    length
                                )
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
                if (trimmedText.isEmpty()) return@let androidx.compose.ui.text.AnnotatedString("")
                raw.subSequence(startOffSet, startOffSet + trimmedText.length)
            }
    }

    fun paginateChapterGreedy(
        text: AnnotatedString,
        textMeasurer: TextMeasurer,
        style: TextStyle,
        containerSize: IntSize
    ): List<AnnotatedString> {
        if (text.text.isBlank() || containerSize.width <= 0 || containerSize.height <= 0) return emptyList()

        val pages = mutableListOf<AnnotatedString>()
        var startIndex = 0
        val textLength = text.length

        while (startIndex < textLength) {
            val remaining = text.subSequence(startIndex, textLength)

            val result = textMeasurer.measure(
                text = remaining,
                style = style,
                constraints = Constraints(maxWidth = containerSize.width)
            )

            var lastFittingLine = -1
            for (line in 0 until result.lineCount) {
                val lineBottom = result.multiParagraph.getLineBottom(line)
                if (lineBottom <= containerSize.height) {
                    lastFittingLine = line
                } else {
                    break
                }
            }

            if (lastFittingLine == -1) lastFittingLine = 0

            val endInRemaining = result.multiParagraph.getLineEnd(lastFittingLine, visibleEnd = true)
            var safeEnd = (startIndex + endInRemaining).coerceIn(startIndex + 1, textLength)

            while (safeEnd < textLength && text.text[safeEnd].isWhitespace() && text.text[safeEnd] != '\n') {
                safeEnd++
            }

            pages.add(text.subSequence(startIndex, safeEnd))
            startIndex = safeEnd
        }
        return pages
    }

    fun cancel() {
        scope.cancel()
        chapterPages.clear()
        pageCounts.clear()
        inProgress.clear()
    }
}
