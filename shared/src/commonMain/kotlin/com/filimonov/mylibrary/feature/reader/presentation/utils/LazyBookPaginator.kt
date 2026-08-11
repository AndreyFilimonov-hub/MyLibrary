package com.filimonov.mylibrary.feature.reader.presentation.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
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

    private val pageCounts = mutableMapOf<Int, Int>()
    var isFullyCounted by mutableStateOf(false)
        private set

    suspend fun countAllPagesInBackground() = coroutineScope {
        val semaphore = Semaphore(4)
        val jobs = chapters.indices.map { chapterIndex ->
            async(Dispatchers.Default) {
                semaphore.withPermit {
                    val count = chapterPages[chapterIndex]?.size ?: run {
                        val annotated = htmlToAnnotatedString(chapters[chapterIndex].content)
                        paginateChapterGreedy(annotated, textMeasurer, style, containerSize).size
                    }
                    pageCounts[chapterIndex] = count
                }
            }
        }
        jobs.awaitAll()
        isFullyCounted = true
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

    fun globalPageIndex(chapterIndex: Int, pageInChapter: Int): Int {
        var acc = 0
        for (i in 0 until chapterIndex) {
            acc += pageCounts[i] ?: 0
        }

        return acc + pageInChapter
    }

    fun totalPages(): Int? {
        if (!isFullyCounted) return null
        return pageCounts.values.sum().coerceAtLeast(1)
    }

    fun cancel() {
        scope.cancel()
        chapterPages.clear()
        pageCounts.clear()
        inProgress.clear()
    }
}
