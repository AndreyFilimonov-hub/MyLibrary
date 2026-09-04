package com.filimonov.mylibrary.data.database.di

import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.data.database.AppDatabase
import com.filimonov.mylibrary.data.database.dao.BookDao
import com.filimonov.mylibrary.data.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.data.database.datasource.BookLocalDataSource
import com.filimonov.mylibrary.data.database.datasource.ReadingProgressLocalDataSource
import com.filimonov.mylibrary.data.database.datasource.impl.BookLocalDataSourceImpl
import com.filimonov.mylibrary.data.database.datasource.impl.ReadingProgressLocalDataSourceImpl
import com.filimonov.mylibrary.data.database.getRoomDatabase
import org.koin.dsl.module

fun databaseModule(
    builder: RoomDatabase.Builder<AppDatabase>
) = module {

    single<AppDatabase> {
        getRoomDatabase(builder)
    }

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
