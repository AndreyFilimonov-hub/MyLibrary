package com.filimonov.mylibrary.feature.library.data.parser.pdf

internal expect suspend fun extractPdfMetadata(bytes: ByteArray): PdfMetadata
