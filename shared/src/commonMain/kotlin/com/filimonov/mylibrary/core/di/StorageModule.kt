package com.filimonov.mylibrary.core.di

import com.filimonov.mylibrary.core.storage.BookStorage
import com.filimonov.mylibrary.core.storage.coverstorage.CoverStorage
import org.koin.dsl.module

val storageModule = module {
    single<CoverStorage> {
        CoverStorage()
    }

    single<BookStorage> {
        BookStorage()
    }
}
