package com.filimonov.mylibrary.core.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PriorityTaskExecutorTest {

    @Test
    fun `when invoke execute task finish without exception`() = runTest {
        val executor = PriorityTaskExecutor(
            strictHighWorkerCount = 0,
            flexibleWorkerCount = 1
        )
        var executionCount = 0

        try {
            executor.execute(TaskPriority.HIGH) {
                executionCount++
            }

            assertEquals(1, executionCount)
        } finally {
            executor.cancel()
        }
    }

    @Test
    fun `failed task does not stop next task`() = runTest {
        val executor = PriorityTaskExecutor(
            strictHighWorkerCount = 0,
            flexibleWorkerCount = 1
        )
        var nextTaskExecuted = false

        try {
            var error: Throwable? = null

            try {
                executor.execute(TaskPriority.HIGH) {
                    error("Test error")
                }
            } catch (t: Throwable) {
                error = t
            }

            assertIs<IllegalStateException>(error)

            executor.execute(TaskPriority.HIGH) {
                nextTaskExecuted = true
            }

            assertTrue(nextTaskExecuted)
        } finally {
            executor.cancel()
        }
    }

    @Test
    fun `background task is executing correctly`() = runTest {
        val executor = PriorityTaskExecutor(
            strictHighWorkerCount = 0,
            flexibleWorkerCount = 1
        )
        var executionCount = 0

        try {
            executor.execute(TaskPriority.BACKGROUND) {
                executionCount++
            }

            assertEquals(1, executionCount)
        } finally {
            executor.cancel()
        }
    }

    @Test
    fun `tasks executing correctly`() = runTest {
        val executor = PriorityTaskExecutor(
            strictHighWorkerCount = 0,
            flexibleWorkerCount = 1
        )
        var executionCount = 0

        try {
            executor.execute(TaskPriority.HIGH) {
                executionCount++
            }
            executor.execute(TaskPriority.HIGH) {
                executionCount++
            }

            assertEquals(2, executionCount)
        } finally {
            executor.cancel()
        }
    }

    @Test
    fun `queued task is cancelled when executor is cancelled`() = runTest {
        val executor = PriorityTaskExecutor(
            strictHighWorkerCount = 0,
            flexibleWorkerCount = 1
        )

        try {
            val started = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val firstTask = async(start = CoroutineStart.UNDISPATCHED) {
                executor.execute(TaskPriority.BACKGROUND) {
                    started.complete(Unit)
                    release.await()
                }
            }
            started.await()
            val queuedTask = async(start = CoroutineStart.UNDISPATCHED) {
                executor.execute(TaskPriority.BACKGROUND) {
                    release.complete(Unit)
                }
            }

            executor.cancel()

            val error = try {
                queuedTask.await()
                null
            } catch (t: Throwable) {
                t
            }
            println(error)
            assertIs<CancellationException>(error)

            firstTask.cancel()
        } finally {
            executor.cancel()
        }
    }
}
