package com.sergebailes.bookbee

import android.app.Application
import com.sergebailes.bookbee.data.database.BookBeeDatabase
import com.sergebailes.bookbee.data.database.BookBeeDatabaseFactory

class BookBeeApplication : Application() {
    val database: BookBeeDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        BookBeeDatabaseFactory.create(this)
    }

    val appContainer: BookBeeAppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        BookBeeAppContainer(database)
    }
}
