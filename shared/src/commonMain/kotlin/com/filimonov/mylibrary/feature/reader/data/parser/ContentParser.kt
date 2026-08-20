package com.filimonov.mylibrary.feature.reader.data.parser

import com.filimonov.mylibrary.feature.reader.domain.model.Chapter

class ContentParser(
    private val epubContentParser: EpubContentParser,
    private val fb2ContentParser: Fb2ContentParser
) {


    suspend fun parseBookContent(
        bookPath: String
    ): List<Chapter> {
        return when {
            bookPath.endsWith(".epub") -> epubContentParser.parseContent(bookPath)
            bookPath.endsWith(".fb2") -> fb2ContentParser.parseContent(bookPath)
            else -> error("Unsupported format: $bookPath")
        }
    }
}
