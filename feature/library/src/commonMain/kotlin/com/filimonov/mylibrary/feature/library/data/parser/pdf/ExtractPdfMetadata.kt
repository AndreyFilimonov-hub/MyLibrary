package com.filimonov.mylibrary.feature.library.data.parser.pdf

expect suspend fun extractPdfMetadata(bytes: ByteArray): PdfMetadata
