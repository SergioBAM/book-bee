package com.sergebailes.bookbee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.sergebailes.bookbee.ui.BookBeeApp
import com.sergebailes.bookbee.ui.history.HistoryViewModel
import com.sergebailes.bookbee.ui.scan.ScanViewModel
import com.sergebailes.bookbee.ui.search.LibrarySearchViewModel
import com.sergebailes.bookbee.ui.shelf.ShelfViewModel
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import com.sergebailes.bookbee.ui.wishlist.WishlistViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as BookBeeApplication).appContainer
        val shelfViewModel = ViewModelProvider(
            this,
            ShelfViewModel.Factory(appContainer),
        )[ShelfViewModel::class.java]
        val wishlistViewModel = ViewModelProvider(
            this,
            WishlistViewModel.Factory(appContainer),
        )[WishlistViewModel::class.java]
        val scanViewModel = ViewModelProvider(
            this,
            ScanViewModel.Factory(appContainer),
        )[ScanViewModel::class.java]
        val historyViewModel = ViewModelProvider(
            this,
            HistoryViewModel.Factory(appContainer),
        )[HistoryViewModel::class.java]
        val librarySearchViewModel = ViewModelProvider(
            this,
            LibrarySearchViewModel.Factory(appContainer),
        )[LibrarySearchViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            BookBeeTheme {
                BookBeeApp(
                    shelfViewModel = shelfViewModel,
                    scanViewModel = scanViewModel,
                    wishlistViewModel = wishlistViewModel,
                    historyViewModel = historyViewModel,
                    librarySearchViewModel = librarySearchViewModel,
                )
            }
        }
    }
}
