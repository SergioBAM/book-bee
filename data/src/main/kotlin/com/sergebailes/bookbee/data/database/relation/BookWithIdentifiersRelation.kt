package com.sergebailes.bookbee.data.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity

data class BookWithIdentifiersRelation(
    @Embedded
    val book: BookEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val identifiers: List<BookIdentifierEntity>,
)
