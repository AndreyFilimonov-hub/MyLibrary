package com.filimonov.mylibrary.data.database.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.data.database.AppDatabase
import com.filimonov.mylibrary.data.database.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule = module {
    single<AppDatabase> {
        getRoomDatabase(getDatabaseBuilder(androidContext()))
    }
}

private fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("book.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
