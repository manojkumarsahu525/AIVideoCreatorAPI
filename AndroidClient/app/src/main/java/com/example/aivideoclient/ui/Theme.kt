package com.example.aivideoclient.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Color(0xFF0066FF),
    primaryVariant = Color(0xFF0047B3),
    secondary = Color(0xFF00BFA6),
)

private val DarkColors = darkColors(
    primary = Color(0xFF90CAF9),
    primaryVariant = Color(0xFF64B5F6),
    secondary = Color(0xFF80CBC4),
)

@Composable
fun AIVideoTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
