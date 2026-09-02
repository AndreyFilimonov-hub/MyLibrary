package com.filimonov.mylibrary.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy.Companion.REPLACE
import androidx.room3.Query
import com.filimonov.mylibrary.data.database.entity.ReadingProgressDbModel

@Dao
interface BookReadingProgressDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(progressDbModel: ReadingProgressDbModel)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun getReadingProgress(bookId: Long): ReadingProgressDbModel?
}
