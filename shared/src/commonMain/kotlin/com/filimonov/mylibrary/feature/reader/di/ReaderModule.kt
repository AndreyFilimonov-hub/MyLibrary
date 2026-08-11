package com.filimonov.mylibrary.feature.reader.di

import com.filimonov.mylibrary.feature.reader.presentation.ReaderViewModel
import com.filimonov.mylibrary.feature.reader.data.parser.ContentParser
import com.filimonov.mylibrary.feature.reader.data.repository.ReaderRepositoryImpl
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookContentByIdUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetReaderSettingsUseCase
import com.filimonov.mylibrary.feature.reader.domain.usecase.SaveSettingsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {

    single<ReaderRepository> {
        ReaderRepositoryImpl(
            bookDao = get(),
            dataStore = get(),
            contentParser = get()
        )
    }

    factory<GetBookContentByIdUseCase> {
        GetBookContentByIdUseCase(get())
    }

    factory<GetReaderSettingsUseCase> {
        GetReaderSettingsUseCase(get())
    }

    factory<SaveSettingsUseCase> {
        SaveSettingsUseCase(get())
    }

    viewModel { (bookId: Long) ->
        ReaderViewModel(
            bookId = bookId,
            getBookUseCase = get(),
            getReaderSettingsUseCase = get(),
            saveSettingsUseCase = get(),
        )
    }

    single<ContentParser> {
        ContentParser()
    }
}
