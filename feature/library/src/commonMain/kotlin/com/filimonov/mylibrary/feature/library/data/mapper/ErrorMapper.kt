package com.filimonov.mylibrary.feature.library.data.mapper

import androidx.sqlite.SQLiteException
import com.filimonov.mylibrary.feature.library.domain.error.LibraryError

fun Throwable.toLibraryError(): LibraryError {
    return when {
        this is SQLiteException ||
                message?.contains("Error code: 2067, message: UNIQUE constraint failed") == true ->
            LibraryError.BookAlreadyExists

        message?.contains("Not a ZIP file") == true ->
            LibraryError.InvalidEpubException

        message?.contains("Unexpected end of ZIP stream") == true ->
            LibraryError.InvalidEpubException

        else ->
            LibraryError.UnknownError
    }
}
