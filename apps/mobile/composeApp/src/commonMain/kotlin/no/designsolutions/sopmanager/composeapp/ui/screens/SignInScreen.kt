package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SignInScreen(onContinue: () -> Unit) {
    Text("SOP Manager", style = MaterialTheme.typography.headlineMedium)
    Text("Auth is mocked on mobile for now.")
    Button(onClick = onContinue) {
        Text("Continue")
    }
}
