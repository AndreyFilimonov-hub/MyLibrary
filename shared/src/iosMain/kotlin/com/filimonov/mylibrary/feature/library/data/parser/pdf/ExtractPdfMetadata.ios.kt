package com.filimonov.mylibrary.feature.library.data.parser.pdf

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFDocumentAuthorAttribute
import platform.PDFKit.PDFDocumentTitleAttribute

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData {
    return usePinned {
        NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
    }
}

actual suspend fun extractPdfMetadata(bytes: ByteArray): PdfMetadata {
    val nsData = bytes.toNSData()
    val document = PDFDocument(nsData)

    val attr = document.documentAttributes

    val title = (attr?.get(PDFDocumentTitleAttribute) as? String)
    val author = (attr?.get(PDFDocumentAuthorAttribute) as? String)

    return PdfMetadata(title, author)
}
