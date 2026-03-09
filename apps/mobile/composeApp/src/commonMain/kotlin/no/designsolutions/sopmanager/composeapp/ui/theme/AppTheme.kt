package no.designsolutions.sopmanager.composeapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1E63B0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE3FF),
    onPrimaryContainer = Color(0xFF0B3558),
    secondary = Color(0xFF245E8E),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFE7EEFC),
    onBackground = Color(0xFF13283B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF13283B),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0B3558),
    primaryContainer = Color(0xFF1E63B0),
    onPrimaryContainer = Color(0xFFEAF3FF),
    secondary = Color(0xFF7EB6E6),
    onSecondary = Color(0xFF123A5B),
    background = Color(0xFF0F1B2A),
    onBackground = Color(0xFFE5EFFC),
    surface = Color(0xFF132236),
    onSurface = Color(0xFFE5EFFC),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        shapes = androidx.compose.material3.Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
        ),
        content = content,
    )
}
