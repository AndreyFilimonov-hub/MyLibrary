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

class EpubContentParser {
    suspend fun parseContent(bookPath: String): List<Chapter> {
        return withContext(Dispatchers.IO) {
            val file = PlatformFile(bookPath)
            val buffer = Buffer().apply { write(file.readBytes()) }

            withContext(Dispatchers.Default) {

                val parsed = EpubReader().readEpub(buffer)

                val resourcesMap = parsed.resources.resourceMap

                val references = parsed.spine.getSpineReferences()

                references.mapIndexedNotNull { index, reference ->
                    val resource = reference.resource ?: return@mapIndexedNotNull null

                    if (resource.href?.contains("cover", ignoreCase = true) == true) {
                        return@mapIndexedNotNull null
                    }

                    val html = resource.data?.decodeToString() ?: return@mapIndexedNotNull null
                    val doc = Ksoup.parse(html, Parser.xmlParser())

                    val title =
                        doc.selectFirst("h1, h2, h3")?.text()?.trim()?.takeIf { it.isNotBlank() }

                    val images = buildMap<String, ByteArray> {
                        doc.select("img[src], image").forEach { img ->
                            val src = img.attr("src")
                                .ifBlank { img.attr("xlink:href") }
                                .ifBlank { img.attr("href") }

                            if (src.isNotBlank()) {
                                resourcesMap[src]?.data?.let { bytes -> put(src, bytes) }
                            }
                        }
                    }

                    val hasMeaningfulText = doc.body().text().isNotBlank()
                    val hasImages = images.isNotEmpty()
                    if (!hasMeaningfulText && !hasImages) {
                        return@mapIndexedNotNull null
                    }

                    Chapter(
                        id = index,
                        title = title,
                        content = html,
                        images = images.ifEmpty { null }
                    )
                }
            }
        }
    }
}
