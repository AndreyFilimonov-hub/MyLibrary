package com.filimonov.mylibrary.data.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "reading_progress",
    primaryKeys = ["bookId"],
    foreignKeys = [
        ForeignKey(
            entity = BookDbModel::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = CASCADE
        )
    ]
)
data class ReadingProgressDbModel(
    val bookId: Long,
    val chapterId: Int,
    val charIndex: Int
)
