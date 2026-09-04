package com.filimonov.mylibrary.data.database.di

import com.filimonov.mylibrary.data.database.AppDatabase
import com.filimonov.mylibrary.data.database.dao.BookDao
import com.filimonov.mylibrary.data.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.data.database.datasource.BookLocalDataSource
import com.filimonov.mylibrary.data.database.datasource.ReadingProgressLocalDataSource
import com.filimonov.mylibrary.data.database.datasource.impl.BookLocalDataSourceImpl
import com.filimonov.mylibrary.data.database.datasource.impl.ReadingProgressLocalDataSourceImpl
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module

val databaseModule = module {

    includes(platformDatabaseModule)

    single<BookDao> {
        get<AppDatabase>().bookDao()
    }

    single<BookReadingProgressDao> {
        get<AppDatabase>().bookReadingProgressDao()
    }

    single<BookLocalDataSource> {
        BookLocalDataSourceImpl(
            bookDao = get()
        )
    }

    single<ReadingProgressLocalDataSource> {
        ReadingProgressLocalDataSourceImpl(
            readingProgressDao = get()
        )
    }
}
