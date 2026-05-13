package com.sergebailes.bookbee.data.database

import androidx.room3.migration.Migration

object BookBeeDatabaseMigrations {
    // Version 1 starts with an explicit migration list so later schema changes
    // can be added without changing the database wiring call sites.
    val ALL: Array<Migration> = emptyArray()
}
