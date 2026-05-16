package com.sergebailes.bookbee.data.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity
import com.sergebailes.bookbee.data.database.entity.WishlistItemEntity

data class WishlistBookRelation(
    @Embedded
    val wishlistItem: WishlistItemEntity,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "id",
    )
    val book: BookEntity,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "bookId",
    )
    val identifiers: List<BookIdentifierEntity>,
)
