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

    suspend fun parseBookContent(bookPath: String): List<Chapter> {
        return withContext(Dispatchers.IO) {

            val file = PlatformFile(bookPath)

            val buffer = Buffer()
            buffer.write(file.readBytes())

            val parsed = EpubReader().readEpub(buffer)

            val chapters = mutableListOf<Chapter>()

            val spine = parsed.spine
            val references = spine.getSpineReferences()

            for ((index, reference) in references.withIndex()) {
                val resource = reference.resource
                val html = resource?.data?.decodeToString() ?: continue

                val doc = Ksoup.parse(html, Parser.xmlParser())
                val bodyText = doc.body().text()

                if (bodyText.isBlank()) continue

                val title = doc.selectFirst("h1, h2, h3")
                    ?.text()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }

                chapters += Chapter(
                    id = index,
                    title = title,
                    content = html
                )
            }

            chapters
        }
    }
}
