package com.filimonov.mylibrary.core.storage

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
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
        val file = PlatformFile(
            FileKit.filesDir,
            "covers/$fileName.jpg"
        )

        file.write(bytes)

        return file.path
    }
}
