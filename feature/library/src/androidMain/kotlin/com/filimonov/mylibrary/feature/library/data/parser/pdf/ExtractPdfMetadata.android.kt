package com.filimonov.mylibrary.feature.library.data.parser.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual suspend fun extractPdfMetadata(bytes: ByteArray): PdfMetadata {
    return withContext(Dispatchers.IO) {
        PDDocument.load(bytes).use { document ->
            val info = document.documentInformation
            PdfMetadata(
                title = info.title?.trim()?.takeIf { it.isNotBlank() },
                author = info.author?.trim()?.takeIf { it.isNotBlank() }
            )
        }
    }
}
