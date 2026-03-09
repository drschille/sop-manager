package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    onScanQr: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    Text("Home", style = MaterialTheme.typography.headlineMedium)
    Button(modifier = Modifier.fillMaxWidth(), onClick = onScanQr) {
        Text("Scan QR")
    }
    Button(modifier = Modifier.fillMaxWidth(), onClick = onSearch) {
        Text("Search by Part Number")
    }
    Button(modifier = Modifier.fillMaxWidth(), onClick = onSettings) {
        Text("Settings")
    }
}
