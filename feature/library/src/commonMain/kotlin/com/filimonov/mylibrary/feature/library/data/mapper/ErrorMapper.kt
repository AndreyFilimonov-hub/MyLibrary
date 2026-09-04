package com.filimonov.mylibrary.feature.library.data.mapper

import com.filimonov.mylibrary.feature.library.domain.error.LibraryError

fun Throwable.toLibraryError(): LibraryError {
    return when {
        message?.contains("Not a ZIP file") == true ->
            LibraryError.InvalidEpubException

        message?.contains("Unexpected end of ZIP stream") == true ->
            LibraryError.InvalidEpubException

        else ->
            LibraryError.UnknownError
    }
}
