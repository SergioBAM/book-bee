package com.sergebailes.bookbee.data.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

object BookBeeDatabaseFactory {
    fun create(context: Context): BookBeeDatabase {
        return Room.databaseBuilder(
            context = context.applicationContext,
            klass = BookBeeDatabase::class.java,
            name = BookBeeDatabase.DATABASE_NAME,
        )
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(*BookBeeDatabaseMigrations.ALL)
            .build()
    }
}
