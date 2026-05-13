package com.sergebailes.bookbee.domain.model

import java.util.UUID

data class BookIdentifier(
    val id: UUID,
    val bookId: UUID,
    val type: IdentifierType,
    val value: String,
)
