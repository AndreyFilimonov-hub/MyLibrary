package com.filimonov.mylibrary.feature.library.data.mapper

import com.filimonov.mylibrary.core.database.entity.BookDbModel
import com.filimonov.mylibrary.feature.library.domain.model.Book


fun BookDbModel.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    path = path,
    coverPath = coverPath,
    isFavorite = isFavorite,
    hash = hash,
    isRead = isRead
)

fun Book.toDbModel() = BookDbModel(
    id = id,
    title = title,
    author = author,
    path = path,
    coverPath = coverPath,
    isFavorite = isFavorite,
    hash = hash,
    isRead = isRead
)