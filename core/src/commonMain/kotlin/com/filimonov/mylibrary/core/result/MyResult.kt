package com.filimonov.mylibrary.core.result

sealed interface MyResult<out T, out E> {

    data class Success<T>(val data: T): MyResult<T, Nothing>

    data class Error<E>(val error: E): MyResult<Nothing, E>
}

inline fun <T, E> MyResult<T, E>.onSuccess(action: (T) -> Unit): MyResult<T, E> {
    if (this is MyResult.Success) {
        action(data)
    }

    return this
}

inline fun <T, E> MyResult<T, E>.onFailure(action: (E) -> Unit): MyResult<T, E> {
    if (this is MyResult.Error) {
        action(error)
    }

    return this
}
