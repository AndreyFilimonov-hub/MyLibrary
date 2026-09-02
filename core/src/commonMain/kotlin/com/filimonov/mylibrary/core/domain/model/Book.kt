package com.filimonov.mylibrary.core.domain.model

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val path: String,
    val coverPath: String?,
    val bookFormat: BookFormat,
    val hash: String,
    val isFavorite: Boolean,
    val isRead: Boolean
)
