package com.filimonov.mylibrary.feature.reader.data.parser

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeBase64

class Fb2ContentParser {
    suspend fun parseContent(bookPath: String): List<Chapter> {
        val bytes = withContext(Dispatchers.IO) {
            PlatformFile(bookPath).readBytes()
        }

        return withContext(Dispatchers.Default) {
            val xmlString = bytes.decodeToString()
            val document = Ksoup.parse(xmlString, Parser.Companion.xmlParser())

            val binaryImages = buildMap<String, ByteArray> {
                document.select("binary[id]").forEach { binary ->
                    val id = binary.attr("id")
                    val bytes = binary.text().decodeBase64()?.toByteArray()
                    runCatching {
                        if (bytes != null) {
                            put(id, bytes)
                        }
                    }
                }
            }

            val sections = document.select("body > section")

            sections.mapIndexed { index, section ->
                val title =
                    section.selectFirst("title")?.text()?.trim()?.takeIf { it.isNotBlank() }

                val images = buildMap<String, ByteArray> {
                    section.select("image").forEach { img ->
                        val href =
                            img.attr("l:href").ifBlank { img.attr("href") }.removePrefix("#")
                        binaryImages[href]?.let { bytes -> put(href, bytes) }
                    }
                }

                val content = """
                        <html>
                            <body>
                                ${section.html()}
                            </body>
                        </html>
                    """.trimIndent()

                Chapter(
                    id = index,
                    title = title,
                    content = content,
                    images = images.ifEmpty { null }
                )
            }
        }
    }
}
