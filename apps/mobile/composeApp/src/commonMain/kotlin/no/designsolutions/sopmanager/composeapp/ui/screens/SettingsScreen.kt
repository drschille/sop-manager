package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    signedInUser: String,
    onTestBackend: () -> Unit,
    onSignOut: () -> Unit,
) {
    Text("Signed in user: $signedInUser")
    Button(onClick = onTestBackend) {
        Text("Test Backend")
    }
    Button(onClick = onSignOut) {
        Text("Sign out")
    }
}
