package com.filimonov.mylibrary.feature.reader.presentation

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter

sealed interface ReaderState {

    data object Loading : ReaderState

    data class Success(
        val chapters: List<Chapter>
    ) : ReaderState
}
