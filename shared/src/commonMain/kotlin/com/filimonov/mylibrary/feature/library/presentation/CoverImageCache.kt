package com.filimonov.mylibrary.feature.library.presentation

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import org.jetbrains.compose.resources.decodeToImageBitmap

class CoverImageCache {

    private val cache = mutableMapOf<String, ImageBitmap>()
    private val mutex = Mutex()

    suspend fun load(path: String?): ImageBitmap? {
        mutex.withLock { cache[path]?.let { return it } }

        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                path?.toPath()?.let {
                    FileSystem.SYSTEM.read(it) {
                        readByteArray()
                    }
                }?.decodeToImageBitmap()
            }.getOrNull()
        }

        if (bitmap != null && path != null) {
            mutex.withLock {
                cache[path] = bitmap
            }
        }

        return bitmap
    }

    suspend fun remove(path: String?) {
        mutex.withLock {
            cache.remove(path)
        }
    }

    fun clear() {
        cache.clear()
    }
}
