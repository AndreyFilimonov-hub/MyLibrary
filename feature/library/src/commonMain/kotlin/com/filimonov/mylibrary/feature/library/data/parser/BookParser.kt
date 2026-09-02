package com.filimonov.mylibrary.feature.library.data.parser

import com.filimonov.mylibrary.core.domain.model.Book
import com.filimonov.mylibrary.core.domain.model.BookFormat
import com.filimonov.mylibrary.feature.library.data.parser.epub.EpubParser
import com.filimonov.mylibrary.feature.library.data.parser.fb2.Fb2Parser
import com.filimonov.mylibrary.feature.library.data.parser.pdf.PdfParser
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class BookParser(
    private val epubParser: EpubParser,
    private val fb2Parser: Fb2Parser,
    private val pdfParser: PdfParser
) {

    suspend fun parseBook(path: String): Book {
        return withContext(Dispatchers.IO) {
            val file = PlatformFile(path)
            val bytes = file.readBytes()

            when (detectFormat(bytes)) {
                BookFormat.EPUB -> epubParser.parseBook(bytes)
                BookFormat.FB2 -> fb2Parser.parseBook(bytes)
                BookFormat.PDF -> pdfParser.parseBook(bytes)
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

        if (
            bytes.size >= 5 &&
            bytes[0] == '%'.code.toByte() &&
            bytes[1] == 'P'.code.toByte() &&
            bytes[2] == 'D'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[4] == '-'.code.toByte()
        ) {
            return BookFormat.PDF
        }

        error("Unsupported book format")
    }
}
