package com.filimonov.mylibrary.feature.library.data.parser

import com.filimonov.mylibrary.feature.library.domain.model.Book
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class BookParser(
    private val epubParser: EpubParser,
    private val fb2Parser: Fb2Parser
) {

    suspend fun parseBook(path: String): Book {
        return withContext(Dispatchers.IO) {
            val file = PlatformFile(path)
            val bytes = file.readBytes()

            when(detectFormat(bytes)) {
                BookFormat.EPUB -> epubParser.parseBook(bytes)
                BookFormat.FB2 -> fb2Parser.parseBook(bytes)
            }
        }
    }

    private fun detectFormat(bytes: ByteArray): BookFormat {
        if (
            bytes.size >= 2 &&
            bytes[0] == 'P'.code.toByte() &&
            bytes[1] == 'K'.code.toByte()
        ) {
            return BookFormat.EPUB
        }

        val header = bytes
            .take(4096)
            .toByteArray()
            .decodeToString()

        if (
            header.contains(
                "<FictionBook",
                ignoreCase = true
            )
        ) {
            return BookFormat.FB2
        }

        error("Unsupported book format")
    }
}
