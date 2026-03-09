package no.designsolutions.timetracker.composeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.designsolutions.timetracker.composeapp.ui.theme.AppTheme
import no.designsolutions.timetracker.composeapp.ui.workstation.WorkStationScreen

@Composable
fun App() {
    AppTheme(darkTheme = isSystemInDarkTheme()) {
        WorkStationScreen(Modifier.background(MaterialTheme.colorScheme.background))
    }
}
