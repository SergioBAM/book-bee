package com.sergebailes.bookbee.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HoneyDark,
    onPrimary = Ink,
    secondary = MossDark,
    onSecondary = Ink,
    tertiary = BarkDark,
    onTertiary = Ink,
    background = PaperDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = Color(0xFF283029),
    onSurfaceVariant = Color(0xFFD5D9D0)
)

private val LightColorScheme = lightColorScheme(
    primary = Honey,
    onPrimary = Color.White,
    secondary = Moss,
    onSecondary = Color.White,
    tertiary = Bark,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6DFD4),
    onSurfaceVariant = Color(0xFF4D4A43)
)

@Composable
fun BookBeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
