package com.filimonov.mylibrary.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.filimonov.mylibrary.core.database.entity.BookDbModel
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY id DESC")
    fun observeBooks(): Flow<List<BookDbModel>>

    @Insert
    suspend fun insert(bookDbModel: BookDbModel)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun delete(id: Long)

    @Update
    suspend fun update(bookDbModel: BookDbModel)

    @Query("SELECT path FROM books WHERE id = :bookId")
    suspend fun getBookFilePath(bookId: Long): String
}
