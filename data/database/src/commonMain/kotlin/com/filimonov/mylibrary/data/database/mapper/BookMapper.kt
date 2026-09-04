package com.filimonov.mylibrary.data.database.mapper

import com.filimonov.mylibrary.data.database.entity.BookDbModel
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.core.domain.model.Book


internal fun BookDbModel.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    path = path,
    coverPath = coverPath,
    bookFormat = BookFormat.valueOf(bookFormat),
    isFavorite = isFavorite,
    hash = hash,
    isRead = isRead
)

internal fun Book.toDbModel() = BookDbModel(
    id = id,
    title = title,
    author = author,
    path = path,
    coverPath = coverPath,
    bookFormat = bookFormat.name,
    isFavorite = isFavorite,
    hash = hash,
    isRead = isRead
)
