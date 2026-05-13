package com.sergebailes.bookbee.domain.model

import com.sergebailes.bookbee.domain.normalization.normalizeAuthors
import com.sergebailes.bookbee.domain.normalization.normalizeTitle
import java.time.Instant
import java.util.UUID

data class Book(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val subtitle: String?,
    val authors: List<String>,
    val normalizedTitle: String = normalizeTitle(title),
    val normalizedAuthors: List<String> = normalizeAuthors(authors),
    val description: String?,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val thumbnailUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
