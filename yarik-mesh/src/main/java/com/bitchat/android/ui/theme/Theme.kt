package com.bitchat.android.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

// Standard UI semantics live in Material so stock components and custom Bitchat composables
// share one source of truth. LocalBitchatPalette below only supplies app-specific extra colors.
internal val DarkBitchatColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF1A4B99),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFFAECBFA),
    onSecondary = Color(0xFF0B1F3A),
    secondaryContainer = Color(0xFF1B334F),
    onSecondaryContainer = Color(0xFFD2E3FC),
    tertiary = DarkBitchatPalette.accentOrange,
    onTertiary = Color.Black,
    background = Color(0xFF0B0C10),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF0B0C10),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF1F2933),
    onSurfaceVariant = Color(0xFFBDC1C6),
    outline = Color(0xFF3C4043),
    outlineVariant = Color(0xFF2A2F34),
    error = Color(0xFFF28B82),
    onError = Color(0xFF410E0B)
)

internal val LightBitchatColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF1967D2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = Color(0xFF041E49),
    tertiary = LightBitchatPalette.accentOrange,
    onTertiary = Color.Black,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF202124),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFE8EAED),
    error = Color(0xFFD93025),
    onError = Color.White
)

@Composable
fun BitchatTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // App-level override from ThemePreferenceManager
    val themePref by ThemePreferenceManager.themeFlow.collectAsState(initial = ThemePreference.System)
    val shouldUseDark = when (darkTheme) {
        true -> true
        false -> false
        null -> when (themePref) {
            ThemePreference.Dark -> true
            ThemePreference.Light -> false
            ThemePreference.System -> isSystemInDarkTheme()
        }
    }

    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (shouldUseDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        shouldUseDark -> DarkBitchatColorScheme
        else -> LightBitchatColorScheme
    }
    val palette = if (shouldUseDark) DarkBitchatPalette else LightBitchatPalette

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (!shouldUseDark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (!shouldUseDark) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    CompositionLocalProvider(LocalBitchatPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
