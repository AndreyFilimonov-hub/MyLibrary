package com.filimonov.mylibrary.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
    tableName = "books"
)
data class BookDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val author: String,

    val path: String,

    val coverPath: String?,

    val isFavorite: Boolean = false,

    val isRead: Boolean = false,

    val currentPage: Int = 0
)
