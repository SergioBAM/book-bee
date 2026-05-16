package com.sergebailes.bookbee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.sergebailes.bookbee.ui.BookBeeApp
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

        enableEdgeToEdge()
        setContent {
            BookBeeTheme {
                BookBeeApp(
                    shelfViewModel = shelfViewModel,
                    wishlistViewModel = wishlistViewModel,
                )
            }
        }
    }
}
