package com.filimonov.mylibrary.feature.library.data.parser

import com.filimonov.mylibrary.core.storage.BookStorage
import com.filimonov.mylibrary.core.storage.CoverStorage
import com.filimonov.mylibrary.feature.library.domain.model.Book
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.toByteString
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Fb2Parser(
    private val bookStorage: BookStorage,
    private val coverStorage: CoverStorage
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun parseBook(bytes: ByteArray): Book {
        return withContext(Dispatchers.Default) {
            val document = Ksoup.parse(
                bytes.decodeToString(),
                Parser.xmlParser()
            )

            val titleInfo = document.selectFirst("description > title-info")

            val title = titleInfo
                ?.selectFirst("book-title")
                ?.text()
                ?.trim()
                .orEmpty()

            val author = document
                .selectFirst("author")
                ?.let { authorElement ->
                    listOfNotNull(
                        authorElement
                            .selectFirst("first-name")
                            ?.text()
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                        authorElement
                            .selectFirst("middle-name")
                            ?.text()
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                        authorElement
                            .selectFirst("last-name")
                            ?.text()
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                    ).joinToString(" ")
                }.orEmpty()

            val coverId = titleInfo
                ?.selectFirst("coverpage image")
                ?.attr("l:href")
                ?.removePrefix("#")

            val coverBytes = coverId?.let { id ->
                document
                    .getElementsByTag("binary")
                    .firstOrNull { it.attr("id") == id }
                    ?.text()
                    ?.filterNot { it.isWhitespace() }
                    ?.let { base64 ->
                        Base64.decode(base64)
                    }
            }

            val hash = bytes
                .toByteString()
                .sha256()
                .hex()

            val coverPath = coverBytes?.let { bytesString ->
                coverStorage.saveCover(
                    bytesString,
                    Uuid.random().toString()
                )
            }

            val bookPath = bookStorage.saveBook(
                bytes,
                BookFormat.FB2,
                Uuid.random().toString()
            )

            Book(
                id = 0,
                title = title,
                author = author,
                path = bookPath,
                coverPath = coverPath,
                hash = hash,
                isFavorite = false,
                isRead = false
            )
        }
    }
}
