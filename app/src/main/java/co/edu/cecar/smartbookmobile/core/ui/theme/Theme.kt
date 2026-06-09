package co.edu.cecar.smartbookmobile.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val LightColorScheme = lightColorScheme(
    primary = CdiRed,
    onPrimary = CdiWhite,
    primaryContainer = CdiRed.copy(alpha = 0.12f),
    onPrimaryContainer = CdiTextDark,
    secondary = CdiBlueDark,
    onSecondary = CdiWhite,
    tertiary = CdiTextSecondary,
    onTertiary = CdiWhite,
    background = CdiBackground,
    onBackground = CdiTextDark,
    surface = CdiSurface,
    onSurface = CdiTextDark,
    surfaceVariant = CdiRowAlt,
    onSurfaceVariant = CdiTextSecondary,
    outline = CdiBorder,
    error = CdiRed,
    onError = CdiWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = CdiRed,
    onPrimary = CdiWhite,
    secondary = CdiBlueDark,
    onSecondary = CdiWhite,
    background = ColorSchemeTokens.darkBackground,
    onBackground = ColorSchemeTokens.darkTextPrimary,
    surface = ColorSchemeTokens.darkSurface,
    onSurface = ColorSchemeTokens.darkTextPrimary,
    surfaceVariant = ColorSchemeTokens.darkSurfaceVariant,
    onSurfaceVariant = ColorSchemeTokens.darkTextSecondary,
    outline = ColorSchemeTokens.darkOutline,
    error = CdiRed,
    onError = CdiWhite,
)

@Composable
fun SmartBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            content()
        }
    }
}

private object ColorSchemeTokens {
    val darkBackground = androidx.compose.ui.graphics.Color(0xFF111318)
    val darkSurface = androidx.compose.ui.graphics.Color(0xFF161A22)
    val darkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF222838)
    val darkTextPrimary = androidx.compose.ui.graphics.Color(0xFFF5F7FA)
    val darkTextSecondary = androidx.compose.ui.graphics.Color(0xFFB4BCC8)
    val darkOutline = androidx.compose.ui.graphics.Color(0xFF3A4152)
}
