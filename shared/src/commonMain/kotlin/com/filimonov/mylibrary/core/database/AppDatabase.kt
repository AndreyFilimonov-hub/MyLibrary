package com.filimonov.mylibrary.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.filimonov.mylibrary.core.database.dao.BookDao
import com.filimonov.mylibrary.core.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.core.database.entity.BookDbModel
import com.filimonov.mylibrary.core.database.entity.ReadingProgressDbModel

@Database(
    entities = [BookDbModel::class, ReadingProgressDbModel::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun bookReadingProgressDao(): BookReadingProgressDao
}
