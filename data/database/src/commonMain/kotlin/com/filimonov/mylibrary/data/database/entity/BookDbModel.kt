package com.filimonov.mylibrary.data.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(
            value = ["hash"], unique = true
        )
    ]
)
data class BookDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val path: String,
    val coverPath: String?,
    val hash: String,
    val bookFormat: String,
    val isFavorite: Boolean = false,
    val isRead: Boolean = false
)
