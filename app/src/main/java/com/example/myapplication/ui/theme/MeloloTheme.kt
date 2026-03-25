package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MeloloDarkScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB9C4FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFBEC6DC),
    tertiary = androidx.compose.ui.graphics.Color(0xFFE0BCFF)
)

private val MeloloLightScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4257B2),
    secondary = androidx.compose.ui.graphics.Color(0xFF575E71),
    tertiary = androidx.compose.ui.graphics.Color(0xFF72548C)
)

@Composable
fun MeloloTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> MeloloDarkScheme
        else -> MeloloLightScheme
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
