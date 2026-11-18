package com.leon.campusgps.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val ColoresCampus = lightColorScheme(
    primary = Color(0xFF087768), onPrimary = Color.White,
    primaryContainer = Color(0xFFD1F2E9), onPrimaryContainer = Color(0xFF063B33),
    secondary = Color(0xFF2F736A), onSecondary = Color.White,
    background = Color(0xFFF5FAF8), surface = Color(0xFFFAFDFB),
    surfaceVariant = Color(0xFFE4F0EB), onSurface = Color(0xFF142D29), onSurfaceVariant = Color(0xFF44625C),
    outline = Color(0xFF69847C), outlineVariant = Color(0xFFCCDFD7),
    error = Color(0xFF9D3434), errorContainer = Color(0xFFFCE5E5), onErrorContainer = Color(0xFF7C2828)
)
private val ColoresNoche = darkColorScheme(
    primary = Color(0xFF83DAC5), onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF155044), onPrimaryContainer = Color(0xFFC0F5E7),
    secondary = Color(0xFFA7D1C4), onSecondary = Color(0xFF183B32),
    background = Color(0xFF0D1B19), surface = Color(0xFF12231F),
    surfaceVariant = Color(0xFF203B33), onSurface = Color(0xFFE1F1E9), onSurfaceVariant = Color(0xFFB0CDC0),
    outline = Color(0xFF8DAA9D), outlineVariant = Color(0xFF36554A),
    error = Color(0xFFFFB4AD), errorContainer = Color(0xFF632A27), onErrorContainer = Color(0xFFFFDAD5)
)
@Composable
fun CampusGPSTheme(oscuro: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (oscuro) ColoresNoche else ColoresCampus, typography = Typography(), content = content)
}
