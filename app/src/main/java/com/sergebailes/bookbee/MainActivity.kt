package com.sergebailes.bookbee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sergebailes.bookbee.ui.BookBeeApp
import com.sergebailes.bookbee.ui.theme.BookBeeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookBeeTheme {
                BookBeeApp()
            }
        }
    }
}
