package com.filimonov.mylibrary.feature.reader.presentation.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.filimonov.mylibrary.core.coroutine.PriorityTaskExecutor
import com.filimonov.mylibrary.core.coroutine.TaskPriority
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.presentation.search.SearchResult
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.parser.Parser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.ceil

class LazyBookPaginator(
    private val chapters: List<Chapter>,
    val style: TextStyle,
    val containerSize: IntSize,
    private val textMeasurer: TextMeasurer,
    private val density: Density
) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val taskExecutor = PriorityTaskExecutor()

    private val _chapterPages = MutableStateFlow<Map<Int, List<AnnotatedString>>>(emptyMap())
    val chapterPages: StateFlow<Map<Int, List<AnnotatedString>>>
        get() = _chapterPages.asStateFlow()

    private val _pageCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())

    private val progressMutex = Mutex()
    private val inProgress = mutableMapOf<Int, CompletableDeferred<List<AnnotatedString>>>()

    private val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val isFullyCounted = MutableStateFlow(false)

    var selectedImage by mutableStateOf<ImageBitmap?>(null)
        private set

    suspend fun countAllPagesInBackground() = coroutineScope {
        val jobs = chapters.indices.map { chapterIndex ->
            async {
                _chapterPages.value[chapterIndex]?.let { pages ->
                    _pageCounts.update {
                        it + (chapterIndex to pages.size)
                    }
                    return@async
                }
                taskExecutor.execute(TaskPriority.BACKGROUND) {
                    _chapterPages.value[chapterIndex]?.let { pages ->
                        _pageCounts.update {
                            it + (chapterIndex to pages.size)
                        }
                        return@execute
                    }
                    val (annotated, placeholders) = chapterToAnnotatedString(chapters[chapterIndex])
                    val pages = paginateChapterGreedy(
                        text = annotated,
                        placeholders = placeholders,
                        textMeasurer = textMeasurer,
                        style = style,
                        containerSize = containerSize
                    )

                    _chapterPages.update {
                        it + (chapterIndex to pages)
                    }

                    _pageCounts.update {
                        it + (chapterIndex to pages.size)
                    }
                }
            }
        }
        jobs.awaitAll()
        isFullyCounted.value = true
    }

    fun getInlineContent(): Map<String, InlineTextContent> {
        return inlineContentMap.toMap()
    }

    fun ensurePaginated(chapterIndex: Int) {
        if (chapterIndex !in chapters.indices) return
        if (_chapterPages.value.containsKey(chapterIndex)) {
            return
        }
        scope.launch {
            ensurePaginatedAwait(chapterIndex)
        }
    }

    suspend fun ensurePaginatedAwait(chapterIndex: Int): List<AnnotatedString> {
        require(chapterIndex in chapters.indices)
        _chapterPages.value[chapterIndex]?.let {
            return it
        }
        val deferred = progressMutex.withLock {
            _chapterPages.value[chapterIndex]?.let {
                return@withLock CompletableDeferred(it)
            }
            inProgress[chapterIndex]?.let {
                return@withLock it
            }
            CompletableDeferred<List<AnnotatedString>>().also {
                inProgress[chapterIndex] = it
            }
        }

        if (deferred.isCompleted) {
            return deferred.await()
        }

        taskExecutor.execute(TaskPriority.HIGH) {
            try {
                _chapterPages.value[chapterIndex]?.let { pages ->
                    deferred.complete(pages)
                    return@execute
                }
                val (annotated, placeholders) = chapterToAnnotatedString(chapters[chapterIndex])
                val pages = paginateChapterGreedy(
                    text = annotated,
                    placeholders = placeholders,
                    textMeasurer = textMeasurer,
                    style = style,
                    containerSize = containerSize
                )

                _chapterPages.update {
                    it + (chapterIndex to pages)
                }
                _pageCounts.update {
                    it + (chapterIndex to pages.size)
                }

                deferred.complete(pages)
            } catch (e: Throwable) {
                deferred.completeExceptionally(e)
                throw e
            } finally {
                progressMutex.withLock {
                    inProgress.remove(chapterIndex)
                }
            }
        }

        return deferred.await()
    }

    fun globalPageIndex(chapterIndex: Int, pageInChapter: Int): Int? {
        val counts = _pageCounts.value

        var acc = 0
        for (i in 0 until chapterIndex) {
            val count = counts[i] ?: return null
            acc += count
        }

        return acc + pageInChapter
    }

    fun totalPages(): Int? {
        if (!isFullyCounted.value) {
            return null
        }
        return _pageCounts.value
            .values
            .sum()
            .coerceAtLeast(1)
    }

    fun searchByQuery(query: String): List<SearchResult> {
        if (!isFullyCounted.value || query.isBlank()) return emptyList()

        val pagesMap = _chapterPages.value

        val result = mutableListOf<SearchResult>()

        for (chapterIndex in chapters.indices) {
            val pages =
                pagesMap[chapterIndex]
                    ?: continue
            pages.forEachIndexed { pageIndex, page ->
                val text = page.text
                var from = 0
                while (true) {
                    val matchIndex =
                        text.indexOf(
                            query,
                            from,
                            ignoreCase = true
                        )

                    if (matchIndex == -1) break

                    val snippetStart = (matchIndex - 30).coerceIn(0, text.length)
                    val snippetEnd =
                        (matchIndex + query.length + 30).coerceIn(snippetStart, text.length)
                    val snippet = "...${text.substring(snippetStart, snippetEnd)}..."

                    result += SearchResult(
                        chapterIndex = chapterIndex,
                        pageIndexInChapter = pageIndex,
                        globalPageIndex =
                            globalPageIndex(
                                chapterIndex,
                                pageIndex
                            ) ?: 0,
                        snippet = snippet
                    )

                    from = matchIndex + query.length
                }
            }
        }

        return result
    }

    fun resolveGlobalPage(globalPageIndex: Int): Pair<Int, Int>? {
        if (!isFullyCounted.value) {
            return null
        }

        val counts = _pageCounts.value

        var acc = 0
        for (chapterIndex in chapters.indices) {
            val count = counts[chapterIndex] ?: return null

            if (globalPageIndex < acc + count) {
                return chapterIndex to (globalPageIndex - acc)
            }
            acc += count
        }
        return chapters.lastIndex to 0
    }

    private fun chapterToAnnotatedString(chapter: Chapter): Pair<AnnotatedString, List<AnnotatedString.Range<Placeholder>>> {
        val document = Ksoup.parse(chapter.content, Parser.xmlParser())
        var imageCounter = 0
        val placeholders = mutableListOf<AnnotatedString.Range<Placeholder>>()

        val blockTags = setOf("section", "blockquote", "li")
        val headerTags = setOf("h1", "h2", "h3", "h4", "h5", "h6")
        val ignoredTags = setOf("script", "style", "head", "title", "meta", "link")

        val paragraphStyle = ParagraphStyle(
            textIndent = TextIndent(
                firstLine = 24.sp
            ),
            lineHeight = style.lineHeight,
            textAlign = TextAlign.Justify,
            hyphens = Hyphens.Auto
        )

        val raw = buildAnnotatedString {
            fun visit(node: Node) {
                when (node) {
                    is TextNode -> {
                        val text = node.text()
                        if (text.isNotBlank()) append(text)
                    }

                    is Element -> {
                        val tag = node.tagName().lowercase()

                        if (tag in ignoredTags) return

                        if (tag == "a" && node.attr("href").isBlank()) return

                        if (tag == "img" || tag == "image") {
                            val src = node.attr("src").ifBlank { node.attr("xlink:href") }
                            val bytes = if (src.isNotBlank()) chapter.images?.get(src) else null

                            val bitmap = bytes?.decodeToImageBitmap()

                            if (bitmap != null) {
                                val id = "chapter_${chapter.id}_img_${imageCounter++}"
                                val (widthSp, heightSp) = calculatePlaceholderSize(bitmap)

                                val placeholder = Placeholder(
                                    width = widthSp,
                                    height = heightSp,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.Top
                                )

                                val start = length
                                appendInlineContent(id, "[image]")
                                val end = length

                                val imageHeightPx = with(density) {
                                    heightSp.toPx()
                                }

                                val lineHeightPx = with(density) {
                                    style.lineHeight.toPx()
                                }

                                val lineCount = ceil(
                                    imageHeightPx / lineHeightPx
                                ).toInt()

                                repeat((lineCount - 1).coerceAtLeast(0)) {
                                    append("\n")
                                }

                                placeholders.add(AnnotatedString.Range(placeholder, start, end))

                                inlineContentMap[id] =
                                    InlineTextContent(placeholder = placeholder) {
                                        Image(
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        onImageClicked(
                                                            bitmap
                                                        )
                                                    },
                                            bitmap = bitmap,
                                            contentDescription =
                                                node.attr("alt"),
                                            contentScale =
                                                ContentScale.Fit
                                        )
                                    }
                            }
                            return
                        }

                        val isImageDiv = tag == "div" && node.selectFirst("img, image") != null

                        val isEmptyLine = tag == "p" && node.classNames().contains("empty-line")

                        val hasNestedBlock =
                            if (tag == "div") {
                                node.children().any { child ->
                                    val childTag = child.tagName().lowercase()
                                    childTag in setOf(
                                        "p",
                                        "div",
                                        "section",
                                        "blockquote",
                                        "li",
                                        "img",
                                        "image",
                                        "h1",
                                        "h2",
                                        "h3",
                                        "h4",
                                        "h5",
                                        "h6"
                                    )
                                }
                            } else {
                                false
                            }

                        val isSimpleDiv = tag == "div" && !isImageDiv && !hasNestedBlock

                        val isParagraph = tag == "p" && !isEmptyLine || isSimpleDiv

                        when {
                            isImageDiv -> {
                                if (length > 0) {
                                    append("\n")
                                }
                            }

                            tag in blockTags || tag in headerTags -> {
                                if (length > 0) {
                                    append("\n\n")
                                }
                            }

                            tag == "br" -> {
                                append("\n")
                            }
                        }

                        val start = length

                        node.childNodes.forEach {
                            visit(it)
                        }

                        when {
                            isParagraph -> addStyle(
                                paragraphStyle,
                                start,
                                length
                            )


                            isImageDiv -> addStyle(
                                ParagraphStyle(textAlign = TextAlign.Center),
                                start,
                                length
                            )

                            tag == "b" || tag == "strong" -> addStyle(
                                SpanStyle(
                                    fontWeight =
                                        FontWeight.Bold
                                ),
                                start,
                                length
                            )

                            tag == "i" || tag == "em" || tag == "cite" -> addStyle(
                                SpanStyle(
                                    fontStyle =
                                        FontStyle.Italic
                                ),
                                start,
                                length
                            )

                            tag == "u" -> addStyle(
                                SpanStyle(
                                    textDecoration =
                                        TextDecoration.Underline
                                ),
                                start,
                                length
                            )

                            tag == "s" || tag == "strike" || tag == "del" -> addStyle(
                                SpanStyle(
                                    textDecoration =
                                        TextDecoration.LineThrough
                                ),
                                start,
                                length
                            )

                            tag == "sub" -> addStyle(
                                SpanStyle(
                                    baselineShift =
                                        BaselineShift.Subscript,
                                    fontSize = 12.sp
                                ),
                                start,
                                length
                            )

                            tag == "sup" -> addStyle(
                                SpanStyle(
                                    baselineShift =
                                        BaselineShift.Superscript,
                                    fontSize = 12.sp
                                ),
                                start,
                                length
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
                                ),
                                start,
                                length
                            )
                            addStyle(
                                ParagraphStyle(
                                    textAlign = TextAlign.Center
                                ),
                                start,
                                length
                            )
                        }

                        val inlineStyle = node.attr("style")
                        if (inlineStyle.isNotBlank()) {
                            if (Regex("font-weight\\s*:\\s*bold").containsMatchIn(inlineStyle)) {
                                addStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    start,
                                    length
                                )
                            }
                            if (Regex("font-weight\\s*:\\s*italic").containsMatchIn(inlineStyle)) {
                                addStyle(
                                    SpanStyle(
                                        fontStyle = FontStyle.Italic
                                    ),
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

        val startOffset = raw.text.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
        val trimmed = raw.let { r ->
            val trimmedText = r.text.trim()

            if (trimmedText.isEmpty()) {
                AnnotatedString("")
            } else {
                r.subSequence(
                    startOffset,
                    startOffset + trimmedText.length
                )
            }
        }

        val adjustedPlaceholders = placeholders
            .mapNotNull { range ->

                val newStart = range.start - startOffset
                val newEnd = range.end - startOffset

                if (newStart >= 0 && newEnd <= trimmed.length) {
                    AnnotatedString.Range(
                        item = range.item,
                        start = newStart,
                        end = newEnd,
                        tag = range.tag
                    )
                } else {
                    null
                }
            }

        return trimmed to adjustedPlaceholders
    }

    private fun paginateChapterGreedy(
        text: AnnotatedString,
        placeholders: List<AnnotatedString.Range<Placeholder>>,
        textMeasurer: TextMeasurer,
        style: TextStyle,
        containerSize: IntSize
    ): List<AnnotatedString> {

        if (text.text.isBlank() || containerSize.width <= 0 || containerSize.height <= 0) {
            return emptyList()
        }

        val pages = mutableListOf<AnnotatedString>()
        val textLength = text.length

        var startIndex = 0

        var windowSize = 12_000

        val minWindowSize = 2_000
        val maxWindowSize = 50_000

        val safetyMarginPx = 16f

        val availableBottom = (containerSize.height - safetyMarginPx).coerceAtLeast(1f)

        val maxImageHeightSp = with(density) {
            availableBottom.toSp()
        }

        while (startIndex < textLength) {
            var windowEnd = minOf(startIndex + windowSize, textLength)

            placeholders
                .firstOrNull { it.start < windowEnd && it.end > windowEnd }
                ?.let { placeholder ->
                    windowEnd = placeholder.end.coerceAtMost(textLength)
                }

            val windowText = text.subSequence(startIndex, windowEnd)

            val windowPlaceholders =
                placeholders
                    .filter { it.start >= startIndex && it.end <= windowEnd }
                    .map { range ->
                        AnnotatedString.Range(
                            item = range.item,
                            start = range.start - startIndex,
                            end = range.end - startIndex,
                            tag = range.tag
                        )
                    }

            val cappedPlaceholders = windowPlaceholders.map { range ->
                if (range.item.height.value > maxImageHeightSp.value) {
                    val scale = maxImageHeightSp.value / range.item.height.value

                    AnnotatedString.Range(
                        item = range.item.copy(
                            width = (range.item.width.value * scale).sp,
                            height = maxImageHeightSp
                        ),
                        start = range.start,
                        end = range.end,
                        tag = range.tag
                    )
                } else {
                    range
                }
            }

            val result = textMeasurer.measure(
                text = windowText,
                style = style,
                placeholders = cappedPlaceholders,
                constraints = Constraints(
                    maxWidth = containerSize.width
                )
            )

            var lastFittingLine = -1

            for (line in 0 until result.lineCount) {
                val lineBottom = result.multiParagraph.getLineBottom(line)

                if (lineBottom <= availableBottom) {
                    lastFittingLine = line
                } else {
                    break
                }
            }

            if (lastFittingLine == result.lineCount - 1 && windowEnd < textLength) {
                windowSize = (windowSize * 2).coerceAtMost(maxWindowSize)
                continue
            }

            if (lastFittingLine == -1) {

                val end = result.multiParagraph.getLineEnd(0, visibleEnd = true).coerceAtLeast(1)

                val absoluteEnd = (startIndex + end).coerceAtMost(textLength)

                pages += text.subSequence(startIndex, absoluteEnd)

                startIndex = absoluteEnd

                continue
            }

            val endInWindow = result.multiParagraph.getLineEnd(lastFittingLine, visibleEnd = true)

            var safeEnd = (startIndex + endInWindow).coerceIn(startIndex + 1, windowEnd)

            result.placeholderRects
                .forEachIndexed { index, rect ->
                    val placeholder =
                        cappedPlaceholders
                            .getOrNull(index)
                            ?: return@forEachIndexed

                    val imageStart =
                        startIndex + placeholder.start

                    if (imageStart >= safeEnd) {
                        return@forEachIndexed
                    }

                    rect?.bottom?.let {
                        if (it > availableBottom) {
                            safeEnd = if (imageStart > startIndex) {
                                imageStart
                            } else {
                                (startIndex + placeholder.end).coerceAtMost(textLength)
                            }
                        }
                    }
                }

            safeEnd = safeEnd.coerceIn(startIndex + 1, textLength)

            pages += text.subSequence(startIndex, safeEnd)

            val consumed = safeEnd - startIndex

            startIndex = safeEnd

            while (
                startIndex < textLength &&
                text.text[startIndex].isWhitespace() &&
                text.text[startIndex] != '\n'
            ) {
                startIndex++
            }

            if (consumed < windowSize / 3) {
                windowSize = (windowSize / 2).coerceAtLeast(minWindowSize)
            } else if (consumed > windowSize * 0.8f) {
                windowSize = (windowSize * 3 / 2).coerceAtMost(maxWindowSize)
            }
        }

        return pages
    }

    private fun calculatePlaceholderSize(bitmap: ImageBitmap): Pair<TextUnit, TextUnit> {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val maxContainerWidthPx = containerSize.width.toFloat()

        val targetWidthPx = minOf(bitmap.width.toFloat(), maxContainerWidthPx)
        val targetHeightPx = targetWidthPx / aspectRatio

        val targetWidthSp = with(density) { targetWidthPx.toSp() }
        val targetHeightSp = with(density) { targetHeightPx.toSp() }

        return targetWidthSp to targetHeightSp
    }

    fun onImageClicked(bitmap: ImageBitmap) {
        selectedImage = bitmap
    }

    fun clearSelectedImage() {
        selectedImage = null
    }

    fun cancel() {
        scope.cancel()
        taskExecutor.cancel()
        _chapterPages.value = emptyMap()
        _pageCounts.value = emptyMap()
        scope.launch {
            progressMutex.withLock {
                inProgress.clear()
            }
        }
        inlineContentMap.clear()
        selectedImage = null
    }
}
