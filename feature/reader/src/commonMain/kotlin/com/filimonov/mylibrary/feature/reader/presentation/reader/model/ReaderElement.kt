package com.filimonov.mylibrary.feature.reader.presentation.reader.model

import androidx.compose.ui.text.AnnotatedString

sealed interface ReaderElement {
    val sourceOffset: Int

    data class Text(
        val value: AnnotatedString,
        override val sourceOffset: Int
    ) : ReaderElement

    data class Image(
        val source: String,
        val bytes: ByteArray,
        val description: String?,
        val widthPx: Int,
        val heightPx: Int,
        override val sourceOffset: Int
    ) : ReaderElement {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false

            return source == other.source &&
                bytes.contentEquals(other.bytes) &&
                description == other.description &&
                widthPx == other.widthPx &&
                heightPx == other.heightPx &&
                sourceOffset == other.sourceOffset
        }

        override fun hashCode(): Int {
            var result = source.hashCode()
            result = 31 * result + bytes.contentHashCode()
            result = 31 * result + (description?.hashCode() ?: 0)
            result = 31 * result + widthPx
            result = 31 * result + heightPx
            result = 31 * result + sourceOffset
            return result
        }
    }
}
