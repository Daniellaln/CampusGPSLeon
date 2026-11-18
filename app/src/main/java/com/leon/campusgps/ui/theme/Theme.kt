package com.leon.campusgps.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresCampus = lightColorScheme(
    primary = Color(0xFF1D709F), onPrimary = Color.White,
    background = Color(0xFFFCFAFF), surface = Color(0xFFFCFAFF),
    surfaceVariant = Color(0xFFEAF1F6), onSurface = Color(0xFF19252D),
    error = Color(0xFF9D3434), errorContainer = Color(0xFFFCE5E5), onErrorContainer = Color(0xFF7C2828)
)
@Composable
fun CampusGPSTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresCampus, typography = Typography(), content = content)
}
