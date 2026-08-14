package com.filimonov.mylibrary.core.di

import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.core.database.AppDatabase
import com.filimonov.mylibrary.core.database.dao.BookDao
import com.filimonov.mylibrary.core.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.core.database.getRoomDatabase
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
}
