package com.filimonov.mylibrary.data.storage.coverstorage

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.write
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

class CoverStorage {

    init {
        val coversDir = "${FileKit.filesDir.path}/covers".toPath()
        FileSystem.SYSTEM.createDirectories(coversDir)
    }

    suspend fun saveCover(
        bytes: ByteArray,
        fileName: String
    ): String {
        val thumbnail = createCoverThumbnail(bytes)

        val file = PlatformFile(
            FileKit.filesDir,
            "covers/$fileName.jpg"
        )

        file.write(thumbnail)

        return file.path
    }

    suspend fun deleteCover(
        path: String?
    ) {
        if (path == null) return

        PlatformFile(path).delete(false)
    }
}
