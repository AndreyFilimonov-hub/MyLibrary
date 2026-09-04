package com.filimonov.mylibrary.data.database.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.data.database.AppDatabase
import com.filimonov.mylibrary.data.database.getRoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformDatabaseModule = module {
    single<AppDatabase> {
        getRoomDatabase(getDatabaseBuilder())
    }
}

private fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/book.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
