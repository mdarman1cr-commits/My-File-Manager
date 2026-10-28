package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF1E88E5),
        secondary = Color(0xFF00B0FF),
        tertiary = Color(0xFF00C853),
        background = Color(0xFFF8F9FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF1F3F4),
        onBackground = Color(0xFF202124),
        onSurface = Color(0xFF202124),
        onSurfaceVariant = Color(0xFF5F6368)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
