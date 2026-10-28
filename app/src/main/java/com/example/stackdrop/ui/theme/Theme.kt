package com.example.stackdrop.ui.theme

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
import com.example.stackdrop.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6C63FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A2B45),
    onPrimaryContainer = Color(0xFFE2E2FF),
    secondary = Color(0xFF00D2D3),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFF9F43),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF453018),
    onTertiaryContainer = Color(0xFFFFD9B3),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFF0F2F5),
    surface = Color(0xFF181B24),
    onSurface = Color(0xFFF0F2F5),
    surfaceVariant = Color(0xFF232733),
    onSurfaceVariant = Color(0xFF9EA3B0),
    error = Color(0xFFFF5252),
    errorContainer = Color(0xFF4D1F24),
    onErrorContainer = Color(0xFFFFD2D2)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5346E0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E5FF),
    onPrimaryContainer = Color(0xFF1D128F),
    secondary = Color(0xFF00A8A8),
    onSecondary = Color.White,
    tertiary = Color(0xFFE67E22),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE8D1),
    onTertiaryContainer = Color(0xFF6E3300),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C21),
    surfaceVariant = Color(0xFFECEFF5),
    onSurfaceVariant = Color(0xFF6B7280),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF8B0000)
)

@Composable
fun StackDropTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        AppTheme.SYSTEM -> isSystemDark
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
