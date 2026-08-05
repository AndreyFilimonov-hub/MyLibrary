package com.filimonov.mylibrary.feature.library.domain.model

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val path: String,
    val coverPath: String?,
    val hash: String,
    val isFavorite: Boolean,
    val isRead: Boolean
)
