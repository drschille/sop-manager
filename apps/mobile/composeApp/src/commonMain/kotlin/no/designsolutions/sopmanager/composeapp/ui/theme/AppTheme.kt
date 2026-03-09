package no.designsolutions.timetracker.composeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1E63B0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE3FF),
    onPrimaryContainer = Color(0xFF0B3558),
    secondary = Color(0xFF245E8E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9EBFF),
    onSecondaryContainer = Color(0xFF123A5B),
    tertiary = Color(0xFF16A34A),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFE7EEFC),
    onBackground = Color(0xFF13283B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF13283B),
    surfaceVariant = Color(0xFFF4F8FF),
    onSurfaceVariant = Color(0xFF4B6179),
    outline = Color(0xFFC8D7EF),
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
    secondaryContainer = Color(0xFF1D4F78),
    onSecondaryContainer = Color(0xFFD9EBFF),
    tertiary = Color(0xFF86EFAC),
    onTertiary = Color(0xFF14532D),
    background = Color(0xFF0F1B2A),
    onBackground = Color(0xFFE5EFFC),
    surface = Color(0xFF132236),
    onSurface = Color(0xFFE5EFFC),
    surfaceVariant = Color(0xFF1C3048),
    onSurfaceVariant = Color(0xFFB5C8E8),
    outline = Color(0xFF4B6179),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A),
)

@Composable
fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
