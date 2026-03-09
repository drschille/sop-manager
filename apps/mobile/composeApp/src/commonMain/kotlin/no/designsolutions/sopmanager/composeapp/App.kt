package no.designsolutions.sopmanager.composeapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.designsolutions.sopmanager.composeapp.qr.QrParser
import no.designsolutions.sopmanager.composeapp.qr.provideQrScanner
import no.designsolutions.sopmanager.composeapp.ui.theme.AppTheme

private enum class Screen {
    SignIn,
    Home,
    Search,
    SopDetail,
    Edit,
    History,
    Settings,
}

@Composable
fun App() {
    AppTheme {
        var screen by remember { mutableStateOf(Screen.SignIn) }
        var partNumber by remember { mutableStateOf("") }
        var sopTitle by remember { mutableStateOf("") }
        var sopBody by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        val photos = remember { mutableStateListOf<String>() }
        val qrScanner = remember { provideQrScanner() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (screen) {
                    Screen.SignIn -> {
                        Text("SOP Manager", style = MaterialTheme.typography.headlineMedium)
                        Text("Sign in with Microsoft to continue.")
                        Button(onClick = {
                            // OIDC hookup belongs in platform-specific auth bridge.
                            statusMessage = "Signed in (placeholder)."
                            screen = Screen.Home
                        }) {
                            Text("Sign in with Microsoft")
                        }
                    }

                    Screen.Home -> {
                        Text("Home", style = MaterialTheme.typography.headlineMedium)
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                qrScanner.scan(
                                    onSuccess = { payload ->
                                        val parsed = QrParser.extractPartNumber(payload)
                                        if (parsed == null) {
                                            statusMessage = "Invalid QR code"
                                        } else {
                                            partNumber = parsed
                                            screen = Screen.SopDetail
                                        }
                                    },
                                    onError = { err -> statusMessage = err },
                                )
                            },
                        ) {
                            Text("Scan QR")
                        }
                        Button(modifier = Modifier.fillMaxWidth(), onClick = { screen = Screen.Search }) {
                            Text("Search by Part Number")
                        }
                        Button(modifier = Modifier.fillMaxWidth(), onClick = { screen = Screen.Settings }) {
                            Text("Settings")
                        }
                    }

                    Screen.Search -> {
                        Text("Search", style = MaterialTheme.typography.headlineMedium)
                        OutlinedTextField(
                            value = partNumber,
                            onValueChange = { partNumber = it },
                            label = { Text("Part number") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(onClick = { screen = Screen.SopDetail }) { Text("Open SOP") }
                        Button(onClick = { screen = Screen.Home }) { Text("Back") }
                    }

                    Screen.SopDetail -> {
                        Text("SOP Detail", style = MaterialTheme.typography.headlineMedium)
                        Text("Part: $partNumber")
                        if (sopTitle.isBlank()) {
                            Text("No SOP found yet")
                            Button(onClick = { screen = Screen.Edit }) { Text("Create SOP") }
                        } else {
                            Text("Title: $sopTitle")
                            Text("Body: $sopBody")
                            Text("Photos: ${photos.size}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { screen = Screen.Edit }) { Text("Edit") }
                                Button(onClick = { screen = Screen.History }) { Text("History") }
                            }
                        }
                        Button(onClick = { screen = Screen.Home }) { Text("Back") }
                    }

                    Screen.Edit -> {
                        Text("Create / Edit SOP", style = MaterialTheme.typography.headlineMedium)
                        OutlinedTextField(
                            value = sopTitle,
                            onValueChange = { sopTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = sopBody,
                            onValueChange = { sopBody = it },
                            label = { Text("Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { photos.add("photo-${photos.size + 1}") }) { Text("Take Photo") }
                            Button(onClick = { if (photos.isNotEmpty()) photos.removeLast() }) { Text("Remove Last") }
                        }
                        if (photos.isNotEmpty()) {
                            LazyColumn {
                                items(photos) { photo ->
                                    Text(photo)
                                }
                            }
                        }
                        Button(onClick = {
                            statusMessage = "Saved as new version (placeholder)."
                            screen = Screen.SopDetail
                        }) {
                            Text("Save SOP")
                        }
                        Button(onClick = { screen = Screen.SopDetail }) { Text("Cancel") }
                    }

                    Screen.History -> {
                        Text("Version History", style = MaterialTheme.typography.headlineMedium)
                        Text("Version 2 - Edited by operator@factory.local")
                        Text("Version 1 - Created by engineer@factory.local")
                        Button(onClick = { screen = Screen.SopDetail }) { Text("Back") }
                    }

                    Screen.Settings -> {
                        Text("Settings", style = MaterialTheme.typography.headlineMedium)
                        Text("Signed in user: placeholder@factory.local")
                        Button(onClick = {
                            statusMessage = "Signed out"
                            screen = Screen.SignIn
                        }) {
                            Text("Sign out")
                        }
                    }
                }

                statusMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
