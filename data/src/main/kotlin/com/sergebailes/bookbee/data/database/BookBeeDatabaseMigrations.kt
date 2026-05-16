package com.sergebailes.bookbee.data.database

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection

object BookBeeDatabaseMigrations {
    val MIGRATION_1_2 = Migration(1, 2) { connection ->
        connection.executeSql(
            """
            CREATE TABLE IF NOT EXISTS `wishlist_items_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `bookId` TEXT NOT NULL,
                `notes` TEXT,
                `createdAt` TEXT NOT NULL,
                `updatedAt` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`userId`) REFERENCES `user_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.executeSql(
            """
            INSERT INTO `wishlist_items_new` (`id`, `userId`, `bookId`, `notes`, `createdAt`, `updatedAt`)
            SELECT `id`, `userId`, `bookId`, `notes`, `createdAt`, `updatedAt`
            FROM `wishlist_items`
            """.trimIndent()
        )
        connection.executeSql("DROP TABLE `wishlist_items`")
        connection.executeSql("ALTER TABLE `wishlist_items_new` RENAME TO `wishlist_items`")
        connection.executeSql(
            "CREATE INDEX IF NOT EXISTS `index_wishlist_items_userId` ON `wishlist_items` (`userId`)"
        )
        connection.executeSql(
            "CREATE INDEX IF NOT EXISTS `index_wishlist_items_bookId` ON `wishlist_items` (`bookId`)"
        )
        connection.executeSql(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_wishlist_items_userId_bookId`
            ON `wishlist_items` (`userId`, `bookId`)
            """.trimIndent()
        )
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}

private fun SQLiteConnection.executeSql(sql: String) {
    prepare(sql).use { statement ->
        statement.step()
    }
}
