package com.filimonov.mylibrary.feature.reader.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filimonov.mylibrary.core.ui.LoadingIndicator
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.presentation.utils.htmlToAnnotatedString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier,
    bookId: Long,
    viewModel: ReaderViewModel = koinViewModel(
        parameters = {
            parametersOf(bookId)
        }
    )
) {
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        val state = viewModel.state.collectAsStateWithLifecycle()

        when (val currentState = state.value) {
            ReaderState.Loading -> LoadingIndicator()
            is ReaderState.Success -> {
                BookReader(
                    modifier = Modifier.padding(innerPadding),
                    chapters = currentState.chapters,
                    settings = ReaderSettings(),
                    restoredProgress = null,
                    onProgressChanged = {}
                )
            }
        }
    }
}

@Composable
fun BookReader(
    modifier: Modifier = Modifier,
    chapters: List<Chapter>,
    settings: ReaderSettings,
    restoredProgress: ReadingProgress?,
    onProgressChanged: (ReadingProgress) -> Unit
) {
    val outerPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { chapters.size }
    )

    LaunchedEffect(outerPagerState.settledPage) {

    }

    HorizontalPager(
        modifier = modifier.fillMaxSize(),
        state = outerPagerState,
        beyondViewportPageCount = 1
    ) { chapterIndex ->
        ChapterPager(
            chapter = chapters[chapterIndex],
            settings = settings,
            restoredCharIndex = 0,
            onPageChanged = {}
        )
    }
}

@Composable
fun ChapterPager(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    settings: ReaderSettings,
    restoredCharIndex: Int?,
    onPageChanged: (charIndex: Int) -> Unit
) {

    val annotatedText = remember(chapter.id) { htmlToAnnotatedString(chapter.content) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val containerSize = with(LocalDensity.current) {
            IntSize(maxWidth.roundToPx(), maxHeight.roundToPx())
        }

        val style = TextStyle(
            fontSize = settings.fontSize,
            lineHeight = settings.lineHeight
        )

        val pages = rememberPaginatedChapter(
            chapterText = annotatedText,
            style = style,
            containerSize = containerSize
        )

        if (pages.isEmpty()) return@BoxWithConstraints

        val pageStartOffsets = remember(pages) {
            val offset = IntArray(pages.size)
            var acc = 0
            pages.forEachIndexed { i, page -> offset[i] = acc; acc += page.length }
            offset
        }

        val initialPage = remember(pages, restoredCharIndex) {
            if (restoredCharIndex == null) 0
            else pageStartOffsets.indexOfLast { it <= restoredCharIndex }.coerceAtLeast(0)
        }

        val innerPagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { pages.size }
        )

        LaunchedEffect(innerPagerState.settledPage, pages) {
            onPageChanged(pageStartOffsets.getOrElse(innerPagerState.settledPage) { 0 })
        }

        HorizontalPager(
            state = innerPagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            Text(
                modifier = Modifier.fillMaxSize(),
                text = pages[pageIndex],
                style = style
            )
        }
    }
}

@Composable
fun rememberPaginatedChapter(
    chapterText: AnnotatedString,
    style: TextStyle,
    containerSize: IntSize
): List<AnnotatedString> {
    val textMeasurer = rememberTextMeasurer()
    val textLength = chapterText.length

    return remember(chapterText, style, containerSize) {
        if (containerSize.width <= 0 || containerSize.height <= 0 || chapterText.text.isBlank()) {
            return@remember if (chapterText.text.isBlank()) emptyList() else listOf(chapterText)
        }

        val pages = mutableListOf<AnnotatedString>()
        var startIndex = 0

        while (startIndex < textLength) {
            var low = startIndex + 1
            var high = textLength
            var bestFit = startIndex

            while (low <= high) {
                val mid = (low + high) / 2
                val result = textMeasurer.measure(
                    text = chapterText.subSequence(
                        startIndex,
                        mid.coerceIn(startIndex, textLength)
                    ),
                    style = style,
                    constraints = Constraints(maxWidth = containerSize.width)
                )
                if (result.size.height <= containerSize.height) {
                    bestFit = mid; low = mid + 1
                } else {
                    high = mid - 1
                }
            }

            if (bestFit == startIndex) bestFit = minOf(startIndex + 1, textLength)

            var safeEnd = findWordBoundary(chapterText.text, bestFit, minIndex = startIndex)

            safeEnd = safeEnd.coerceIn(startIndex + 1, textLength)

            pages.add(chapterText.subSequence(startIndex, safeEnd))
            startIndex = safeEnd
        }

        pages
    }
}

private fun findWordBoundary(text: String, index: Int, minIndex: Int): Int {
    val safeIndex = index.coerceIn(0, text.length)
    if (safeIndex >= text.length) return text.length
    var i = safeIndex
    while (i > minIndex && !text[i].isWhitespace()) i--
    return if (i <= minIndex) safeIndex else i
}
