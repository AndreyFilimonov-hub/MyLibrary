package com.filimonov.mylibrary.feature.library.data.parser.pdf

import io.github.vinceglb.filekit.utils.toNSData
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFDocumentAuthorAttribute
import platform.PDFKit.PDFDocumentTitleAttribute

internal actual suspend fun extractPdfMetadata(bytes: ByteArray): PdfMetadata {
    val nsData = bytes.toNSData()
    val document = PDFDocument(nsData)

    val attr = document.documentAttributes

    val title = (attr?.get(PDFDocumentTitleAttribute) as? String)
    val author = (attr?.get(PDFDocumentAuthorAttribute) as? String)

    return PdfMetadata(title, author)
}
