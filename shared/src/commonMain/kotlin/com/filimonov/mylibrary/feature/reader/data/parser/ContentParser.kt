package com.filimonov.mylibrary.feature.reader.data.parser

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.parser.Parser
import io.documentnode.epub4kmp.epub.EpubReader
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.Buffer

class ContentParser {


    suspend fun parseBookContent(
        bookPath: String
    ): List<Chapter> = withContext(Dispatchers.IO) {

        val file = PlatformFile(bookPath)

        val buffer = Buffer().apply {
            write(file.readBytes())
        }

        val parsed = EpubReader().readEpub(buffer)

        val resourcesMap = parsed.resources.resourceMap

        val references = parsed.spine.getSpineReferences()

        val chapters =
            references.mapIndexedNotNull { index, reference ->

                val resource = reference.resource ?: return@mapIndexedNotNull null

                val html = resource.data?.decodeToString() ?: return@mapIndexedNotNull null

                val doc = Ksoup.parse(html, Parser.xmlParser())

                val title = doc.selectFirst("h1, h2, h3")
                        ?.text()
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }

                val images =
                    buildMap<String, ByteArray> {
                        doc.select("img[src]")
                            .forEach { img ->
                                val src = img.attr("src")

                                resourcesMap[src]
                                    ?.data
                                    ?.let { bytes -> put(src, bytes) }
                            }
                    }

                Chapter(
                    id = index,
                    title = title,
                    content = html,
                    images = images.ifEmpty { null }
                )
            }

        chapters
    }
}
