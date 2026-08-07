package com.filimonov.mylibrary.feature.reader.di

import com.filimonov.mylibrary.feature.reader.data.parser.ContentParser
import com.filimonov.mylibrary.feature.reader.data.repository.ReaderRepositoryImpl
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository
import com.filimonov.mylibrary.feature.reader.domain.usecase.GetBookContentByIdUseCase
import com.filimonov.mylibrary.feature.reader.presentation.ReaderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {

    single<ReaderRepository> {
        ReaderRepositoryImpl(
            bookDao = get(),
            contentParser = get()
        )
    }

    factory<GetBookContentByIdUseCase> {
        GetBookContentByIdUseCase(get())
    }

    viewModel { (bookId: Long) ->
        ReaderViewModel(
            bookId = bookId,
            getBookContentByIdUseCase = get()
        )
    }

    single<ContentParser> {
        ContentParser()
    }
}
