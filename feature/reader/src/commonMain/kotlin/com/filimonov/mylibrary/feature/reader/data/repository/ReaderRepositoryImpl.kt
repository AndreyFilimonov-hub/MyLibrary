package com.filimonov.mylibrary.feature.reader.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.data.database.dao.BookDao
import com.filimonov.mylibrary.data.database.dao.BookReadingProgressDao
import com.filimonov.mylibrary.data.database.mapper.toDomain
import com.filimonov.mylibrary.feature.reader.data.mapper.toDbModel
import com.filimonov.mylibrary.feature.reader.data.mapper.toDomain
import com.filimonov.mylibrary.feature.reader.data.parser.ContentParser
import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderSettings
import com.filimonov.mylibrary.feature.reader.domain.model.ReaderTheme
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingMode
import com.filimonov.mylibrary.feature.reader.domain.model.ReadingProgress
import com.filimonov.mylibrary.feature.reader.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReaderRepositoryImpl(
    private val bookDao: BookDao,
    private val bookReadingProgressDao: BookReadingProgressDao,
    private val dataStore: DataStore<Preferences>,
    private val contentParser: ContentParser
) : ReaderRepository {

    private object KEYS {
        val FONT_SIZE = intPreferencesKey("font_size")
        val LINE_HEIGHT = intPreferencesKey("line_height")
        val READING_MODE = stringPreferencesKey("reading_mode")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val THEME = stringPreferencesKey("theme ")
    }

    override suspend fun getBookById(bookId: Long): Book {
        return bookDao.getBookById(bookId).toDomain()
    }

    override suspend fun getBookContentById(bookId: Long): List<Chapter> {
        val bookPath = bookDao.getBookFilePath(bookId)

        return contentParser.parseBookContent(bookPath)
    }

    override fun getReaderSettings(): Flow<ReaderSettings> {
        return dataStore.data.map { prefs ->
            ReaderSettings(
                fontSize = prefs[KEYS.FONT_SIZE] ?: 18,
                lineHeight = prefs[KEYS.LINE_HEIGHT] ?: 24,
                readingMode = prefs[KEYS.READING_MODE]?.let { ReadingMode.valueOf(it) }
                    ?: ReadingMode.HORIZONTAL,
                brightness = prefs[KEYS.BRIGHTNESS] ?: 100f,
                theme = prefs[KEYS.THEME]?.let { ReaderTheme.valueOf(it) } ?: ReaderTheme.Light
            )
        }
    }

    override suspend fun saveReaderSettings(settings: ReaderSettings) {
        dataStore.edit { prefs ->
            prefs[KEYS.FONT_SIZE] = settings.fontSize
            prefs[KEYS.LINE_HEIGHT] = settings.lineHeight
            prefs[KEYS.READING_MODE] = settings.readingMode.name
            prefs[KEYS.BRIGHTNESS] = settings.brightness
            prefs[KEYS.THEME] = settings.theme.name
        }
    }

    override suspend fun getReadingProgress(bookId: Long): ReadingProgress? {
        return bookReadingProgressDao.getReadingProgress(bookId)?.toDomain()
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress) {
        bookReadingProgressDao.insert(progress.toDbModel())
    }
}
