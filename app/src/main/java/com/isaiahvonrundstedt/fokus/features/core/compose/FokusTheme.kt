package com.isaiahvonrundstedt.fokus.features.core.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val colorLightBlue500 = Color(0xFF2197FE)
val colorLightBlue700 = Color(0xFF2076DC)

@Composable
fun FokusTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppLightColorScheme) {
        content()
    }
}

private val AppLightColorScheme = lightColors(
    primary = colorLightBlue500,
    onPrimary = Color.White,
    secondary = colorLightBlue500,
    onSecondary = Color.White,
    surface = Color.White
)

object FokusTheme {
    val colors: Colors = AppLightColorScheme
}