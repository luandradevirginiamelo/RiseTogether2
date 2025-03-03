package com.example.risetogether.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*

import androidx.compose.ui.graphics.Color

// Esquema de cores para Modo Claro
private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFFA5D6A7),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Esquema de cores para Modo Escuro
private val DarkColors = darkColorScheme(
    primary = Color(0xFF1B5E20),
    secondary = Color(0xFF4CAF50),
    background = Color.Black,
    surface = Color.DarkGray,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// Variável para alternar o tema
var isDarkTheme = mutableStateOf(false)

@Composable
fun RisetogetherTheme(content: @Composable () -> Unit) {
    val darkTheme by remember { isDarkTheme }

    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}


