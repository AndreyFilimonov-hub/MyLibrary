package com.filimonov.mylibrary.feature.reader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookContentByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val bookId: Long,
    private val getBookContentByIdUseCase: GetBookContentByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val chapters = getBookContentByIdUseCase(bookId)
            _state.update {
                ReaderState.Success(chapters)
            }
        }
    }
}
