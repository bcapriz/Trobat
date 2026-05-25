package com.trobat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TrobatPurple,
    onPrimary = Color.White,

    primaryContainer = TrobatPurpleSoft,
    onPrimaryContainer = TrobatPurpleDark,

    secondary = TrobatBlue,
    onSecondary = Color.White,

    secondaryContainer = TrobatBlueSoft,
    onSecondaryContainer = TrobatBlue,

    tertiary = TrobatGreen,
    onTertiary = Color.White,

    error = TrobatRed,
    onError = Color.White,

    background = TrobatBackground,
    onBackground = TrobatText,

    surface = TrobatSurface,
    onSurface = TrobatText,

    surfaceVariant = TrobatCard,
    onSurfaceVariant = TrobatTextSecondary,

    outline = TrobatOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = TrobatPurpleSoft,
    onPrimary = TrobatPurpleDark,

    primaryContainer = TrobatPurple,
    onPrimaryContainer = Color.White,

    secondary = TrobatBlueSoft,
    onSecondary = TrobatBlue,

    secondaryContainer = TrobatBlue,
    onSecondaryContainer = Color.White,

    tertiary = TrobatGreen,
    onTertiary = Color.White,

    error = TrobatRed,
    onError = Color.White,

    background = BackgroundPrincipal,
    onBackground = Color.White,

    surface = TrobatPurpleDark,
    onSurface = Color.White,

    surfaceVariant = TrobatPurple,
    onSurfaceVariant = TrobatPurpleSoft,

    outline = TrobatOutline
)

@Composable
fun TrobatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}