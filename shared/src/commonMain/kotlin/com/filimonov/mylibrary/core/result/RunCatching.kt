package com.filimonov.mylibrary.core.result

import kotlinx.coroutines.CancellationException

inline fun <T, E> runCatching(
    mapError: (Throwable) -> E,
    block: () -> T
): MyResult<T, E> {
    return try {
        MyResult.Success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        MyResult.Error(mapError(e))
    }
}
