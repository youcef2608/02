package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = AtharTealPrimaryDark,
    onPrimary = AtharNavyTertiary,
    primaryContainer = AtharTealContainerDark,
    onPrimaryContainer = AtharTealLight,
    secondary = AtharAmberSecondary,
    secondaryContainer = AtharAmberContainer,
    onSecondaryContainer = AtharOnAmberContainer,
    tertiary = AtharTealLight,
    background = AtharBackgroundDark,
    surface = AtharSurfaceDark,
    surfaceVariant = AtharSurfaceVariantDark,
    onBackground = AtharTextPrimaryDark,
    onSurface = AtharTextPrimaryDark,
    onSurfaceVariant = AtharTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = AtharTealPrimary,
    onPrimary = AtharSurfaceLight,
    primaryContainer = AtharTealContainer,
    onPrimaryContainer = AtharOnTealContainer,
    secondary = AtharAmberSecondary,
    secondaryContainer = AtharAmberContainer,
    onSecondaryContainer = AtharOnAmberContainer,
    tertiary = AtharNavyTertiary,
    background = AtharBackgroundLight,
    surface = AtharSurfaceLight,
    surfaceVariant = AtharSurfaceVariantLight,
    onBackground = AtharTextPrimary,
    onSurface = AtharTextPrimary,
    onSurfaceVariant = AtharTextSecondary
)

@Composable
fun AtharTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Athar is an authentic Arabic platform, always defaulting to RTL layout direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
