package com.filimonov.mylibrary.core.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PriorityTaskExecutor(
    private val strictHighWorkerCount: Int = 2,
    private val flexibleWorkerCount: Int = 2
) {
    private data class Task(
        val priority: TaskPriority,
        val block: suspend () -> Unit,
        val completion: CompletableDeferred<Unit>
    )

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val mutex = Mutex()

    private val highQueue = ArrayDeque<Task>()
    private val backgroundQueue = ArrayDeque<Task>()

    private val highSignal = Channel<Unit>(Channel.CONFLATED)
    private val generalSignal = Channel<Unit>(Channel.CONFLATED)

    init {
        repeat(strictHighWorkerCount) {
            scope.launch {
                strictHighWorkerLoop()
            }
        }
        repeat(flexibleWorkerCount) {
            scope.launch {
                flexibleWorkerLoop()
            }
        }
    }

    suspend fun execute(
        priority: TaskPriority,
        block: suspend () -> Unit
    ) {
        val completion = CompletableDeferred<Unit>()

        mutex.withLock {
            val task = Task(
                priority = priority,
                block = {
                    try {
                        block()
                        completion.complete(Unit)
                    } catch (e: CancellationException) {
                        completion.completeExceptionally(e)
                        throw e
                    } catch (e: Throwable) {
                        completion.completeExceptionally(e)
                    }
                },
                completion = completion
            )

            when(priority) {
                TaskPriority.HIGH -> highQueue.add(task)
                TaskPriority.BACKGROUND -> backgroundQueue.add(task)
            }
        }

        highSignal.trySend(Unit)
        generalSignal.trySend(Unit)

        completion.await()
    }

    private suspend fun strictHighWorkerLoop() {
        while (currentCoroutineContext().isActive) {
            val task = mutex.withLock { highQueue.removeFirstOrNull() }
            if (task == null) {
                highSignal.receive()
                continue
            }
            try { task.block() } catch (e: CancellationException) { throw e }
        }
    }

    private suspend fun flexibleWorkerLoop() {
        while (currentCoroutineContext().isActive) {
            val task = mutex.withLock {
                highQueue.removeFirstOrNull() ?: backgroundQueue.removeFirstOrNull()
            }
            if (task == null) {
                generalSignal.receive()
                continue
            }
            try { task.block() } catch (e: CancellationException) { throw e }
        }
    }

    fun cancel() {
        scope.launch {
            val tasks = mutex.withLock {
                val queuedTasks = highQueue.toList() + backgroundQueue.toList()
                highQueue.clear()
                backgroundQueue.clear()
                queuedTasks
            }
            tasks.forEach { it.completion.cancel() }
            cancel()
        }
    }
}
