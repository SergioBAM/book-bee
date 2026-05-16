package com.sergebailes.bookbee.data.repository.mapper

import com.sergebailes.bookbee.data.database.entity.BookEntity
import com.sergebailes.bookbee.data.database.entity.BookIdentifierEntity
import com.sergebailes.bookbee.data.database.entity.IdentifierType
import com.sergebailes.bookbee.data.database.entity.OwnershipEntity
import com.sergebailes.bookbee.data.database.entity.OwnershipStatus
import com.sergebailes.bookbee.data.database.entity.ReadStatus
import com.sergebailes.bookbee.data.database.entity.UserProfileEntity
import com.sergebailes.bookbee.data.database.entity.WishlistItemEntity
import com.sergebailes.bookbee.data.database.relation.ShelfBookRelation
import com.sergebailes.bookbee.data.database.relation.WishlistBookRelation
import com.sergebailes.bookbee.domain.model.Book
import com.sergebailes.bookbee.domain.model.BookIdentifier
import com.sergebailes.bookbee.domain.model.Ownership
import com.sergebailes.bookbee.domain.model.ShelfBook
import com.sergebailes.bookbee.domain.model.UserProfile
import com.sergebailes.bookbee.domain.model.WishlistBook
import com.sergebailes.bookbee.domain.model.WishlistItem

fun UserProfileEntity.toDomainModel(): UserProfile {
    return UserProfile(
        id = id,
        displayName = displayName,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun UserProfile.toDataModel(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        displayName = displayName,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun BookEntity.toDomainModel(): Book {
    return Book(
        id = id,
        userId = userId,
        title = title,
        subtitle = subtitle,
        authors = authors,
        normalizedTitle = normalizedTitle,
        normalizedAuthors = normalizedAuthors,
        description = description,
        publisher = publisher,
        publishedDate = publishedDate,
        pageCount = pageCount,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Book.toDataModel(): BookEntity {
    return BookEntity(
        id = id,
        userId = userId,
        title = title,
        subtitle = subtitle,
        authors = authors,
        normalizedTitle = normalizedTitle,
        normalizedAuthors = normalizedAuthors,
        description = description,
        publisher = publisher,
        publishedDate = publishedDate,
        pageCount = pageCount,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun BookIdentifierEntity.toDomainModel(): BookIdentifier {
    return BookIdentifier(
        id = id,
        bookId = bookId,
        type = com.sergebailes.bookbee.domain.model.IdentifierType.valueOf(type.name),
        value = value,
    )
}

fun BookIdentifier.toDataModel(): BookIdentifierEntity {
    return BookIdentifierEntity(
        id = id,
        bookId = bookId,
        type = IdentifierType.valueOf(type.name),
        value = value,
    )
}

fun OwnershipEntity.toDomainModel(): Ownership {
    return Ownership(
        id = id,
        userId = userId,
        bookId = bookId,
        quantity = quantity,
        status = com.sergebailes.bookbee.domain.model.OwnershipStatus.valueOf(status.name),
        readStatus = com.sergebailes.bookbee.domain.model.ReadStatus.valueOf(readStatus.name),
        dateAdded = dateAdded,
        archivedAt = archivedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Ownership.toDataModel(): OwnershipEntity {
    return OwnershipEntity(
        id = id,
        userId = userId,
        bookId = bookId,
        quantity = quantity,
        status = OwnershipStatus.valueOf(status.name),
        readStatus = ReadStatus.valueOf(readStatus.name),
        dateAdded = dateAdded,
        archivedAt = archivedAt,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun ShelfBookRelation.toDomainModel(): ShelfBook {
    return ShelfBook(
        book = book.book.toDomainModel(),
        ownership = ownership.toDomainModel(),
        identifiers = book.identifiers.map(BookIdentifierEntity::toDomainModel),
    )
}

fun WishlistItemEntity.toDomainModel(): WishlistItem {
    return WishlistItem(
        id = id,
        userId = userId,
        bookId = bookId,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun WishlistItem.toDataModel(): WishlistItemEntity {
    return WishlistItemEntity(
        id = id,
        userId = userId,
        bookId = bookId,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun WishlistBookRelation.toDomainModel(): WishlistBook {
    return WishlistBook(
        item = wishlistItem.toDomainModel(),
        book = book.toDomainModel(),
        identifiers = identifiers.map(BookIdentifierEntity::toDomainModel),
    )
}
