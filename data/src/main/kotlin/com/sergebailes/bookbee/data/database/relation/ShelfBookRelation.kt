package com.sergebailes.bookbee.data.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.OwnershipEntity

data class ShelfBookRelation(
    @Embedded
    val ownership: OwnershipEntity,
    @Relation(
        entity = BookEntity::class,
        parentColumn = "bookId",
        entityColumn = "id",
    )
    val book: BookWithIdentifiersRelation,
)
