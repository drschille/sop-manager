package no.designsolutions.sopmanager.composeapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.SopVersion
import no.designsolutions.sopmanager.composeapp.qr.QrParser
import no.designsolutions.sopmanager.composeapp.qr.provideQrScanner
import no.designsolutions.sopmanager.composeapp.repository.ConvexSopRepository
import no.designsolutions.sopmanager.composeapp.repository.PhotoPayload
import no.designsolutions.sopmanager.composeapp.repository.SopRepository
import no.designsolutions.sopmanager.composeapp.ui.theme.AppTheme

private const val DefaultConvexUrl = "https://dynamic-fish-439.convex.cloud"

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
    AppTheme(isSystemInDarkTheme()) {
        var screen by retain { mutableStateOf(Screen.SignIn) }
        var convexUrl by retain { mutableStateOf(DefaultConvexUrl) }
        val repository: SopRepository = retain(convexUrl) { ConvexSopRepository(convexUrl) }

        var partNumber by retain { mutableStateOf("") }
        var currentDetail by retain { mutableStateOf<ProcedureDetail?>(null) }
        var sopTitle by retain { mutableStateOf("") }
        var sopBody by retain { mutableStateOf("") }
        var selectedVersion by retain { mutableStateOf<SopVersion?>(null) }

        val versionList = retain { mutableStateListOf<SopVersion>() }
        val searchResults = retain { mutableStateListOf<PartSearchResult>() }
        val editPhotoDescriptions = retain { mutableStateListOf<String>() }

        var signedInUser by retain { mutableStateOf("mock-user@local") }
        var loading by retain { mutableStateOf(false) }
        var statusMessage by retain { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val qrScanner = retain { provideQrScanner() }
        var detailSubscription by retain { mutableStateOf<Job?>(null) }
        var searchSubscription by retain { mutableStateOf<Job?>(null) }

        fun loadProcedureByPart(targetPartNumber: String, onLoaded: (() -> Unit)? = null) {
            detailSubscription?.cancel()
            detailSubscription = scope.launch {
                loading = true
                statusMessage = null
                partNumber = targetPartNumber.trim()
                var opened = false
                try {
                    repository.observeByPartNumber(targetPartNumber.trim()).collect { detail ->
                        currentDetail = detail
                        val latest = detail?.latest
                        sopTitle = latest?.title.orEmpty()
                        sopBody = latest?.body.orEmpty()
                        selectedVersion = latest
                        if (!opened) {
                            onLoaded?.invoke()
                            opened = true
                        }
                        loading = false
                    }
                } catch (error: Throwable) {
                    statusMessage = error.message ?: "Failed to load SOP"
                    loading = false
                }
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator()
                }

                when (screen) {
                    Screen.SignIn -> {
                        Text("SOP Manager", style = MaterialTheme.typography.headlineMedium)
                        Text("Auth is mocked on mobile for now.")
                        Button(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    signedInUser = try {
                                        repository.observeCurrentUserEmail().first() ?: "mock-user@local"
                                    } catch (_: Throwable) {
                                        "mock-user@local"
                                    }
                                    loading = false
                                    statusMessage = "Signed in (mocked auth)"
                                    screen = Screen.Home
                                }
                            },
                        ) {
                            Text("Continue")
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
                                            loadProcedureByPart(parsed) { screen = Screen.SopDetail }
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                searchSubscription?.cancel()
                                scope.launch {
                                    loading = true
                                    statusMessage = null
                                    try {
                                        searchSubscription = launch {
                                            repository.observeSearchParts(partNumber).collect { results ->
                                                searchResults.clear()
                                                searchResults.addAll(results)
                                                loading = false
                                            }
                                        }
                                    } catch (error: Throwable) {
                                        statusMessage = error.message ?: "Search failed"
                                        loading = false
                                    }
                                }
                            }) {
                                Text("Search")
                            }
                            Button(onClick = { screen = Screen.Home }) { Text("Back") }
                        }

                        if (searchResults.isNotEmpty()) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(searchResults) { result ->
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            loadProcedureByPart(result.partNumber) { screen = Screen.SopDetail }
                                        },
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(result.partNumber)
                                            Text(result.sopTitle ?: "No SOP title yet")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Screen.SopDetail -> {
                        Text("SOP Detail", style = MaterialTheme.typography.headlineMedium)
                        Text("Part: $partNumber")

                        val latest = currentDetail?.latest
                        if (latest == null) {
                            Text("No SOP found yet")
                            Button(onClick = {
                                sopTitle = ""
                                sopBody = ""
                                editPhotoDescriptions.clear()
                                screen = Screen.Edit
                            }) {
                                Text("Create SOP")
                            }
                        } else {
                            Text("Title: ${latest.title}")
                            Text("Body: ${latest.body}")
                            Text("Photos: ${latest.photos.size}")
                            if (latest.photos.isNotEmpty()) {
                                latest.photos.forEachIndexed { index, photo ->
                                    val description = photo.description?.takeIf { it.isNotBlank() } ?: "No instruction"
                                    Text("Step ${index + 1}: $description")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    sopTitle = latest.title
                                    sopBody = latest.body
                                    editPhotoDescriptions.clear()
                                    editPhotoDescriptions.addAll(latest.photos.map { it.description.orEmpty() })
                                    screen = Screen.Edit
                                }) { Text("Edit") }
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

                        if (editPhotoDescriptions.isNotEmpty()) {
                            Text("Photo steps")
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(editPhotoDescriptions.size) { index ->
                                    OutlinedTextField(
                                        value = editPhotoDescriptions[index],
                                        onValueChange = { editPhotoDescriptions[index] = it },
                                        label = { Text("Step ${index + 1} description") },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        Button(onClick = {
                            scope.launch {
                                loading = true
                                statusMessage = null
                                try {
                                    val photos = editPhotoDescriptions
                                        .filter { it.isNotBlank() }
                                        .map { PhotoPayload(storageId = "", description = it) }
                                        .filter { it.storageId.isNotBlank() }

                                    val procedureId = currentDetail?.procedureId
                                    if (procedureId.isNullOrBlank()) {
                                        repository.saveNew(partNumber, sopTitle, sopBody, emptyList())
                                    } else {
                                        repository.saveEdit(procedureId, sopTitle, sopBody, photos)
                                    }
                                    statusMessage = "SOP saved"
                                    loadProcedureByPart(partNumber) {
                                        screen = Screen.SopDetail
                                    }
                                } catch (error: Throwable) {
                                    statusMessage = error.message ?: "Save failed"
                                } finally {
                                    loading = false
                                }
                            }
                        }) {
                            Text("Save SOP")
                        }
                        Button(onClick = { screen = Screen.SopDetail }) { Text("Cancel") }
                    }

                    Screen.History -> {
                        Text("Version History", style = MaterialTheme.typography.headlineMedium)
                        LaunchedEffect(currentDetail?.procedureId) {
                            val procedureId = currentDetail?.procedureId ?: return@LaunchedEffect
                            loading = true
                            try {
                                repository.observeVersions(procedureId).collect { versions ->
                                    versionList.clear()
                                    versionList.addAll(versions)
                                    loading = false
                                }
                            } catch (error: Throwable) {
                                statusMessage = error.message ?: "Failed to load versions"
                                loading = false
                            }
                        }

                        if (versionList.isEmpty()) {
                            Text("No versions found")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(versionList) { version ->
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            scope.launch {
                                                loading = true
                                                selectedVersion = try {
                                                    repository.observeVersion(version.id).first()
                                                } catch (error: Throwable) {
                                                    statusMessage = error.message ?: "Failed to load version"
                                                    null
                                                }
                                                loading = false
                                            }
                                        },
                                    ) {
                                        Text("v${version.versionNumber} - ${version.title} (${version.createdBy})")
                                    }
                                }
                            }
                        }

                        selectedVersion?.let { version ->
                            Text("Selected: v${version.versionNumber}")
                            Text(version.body)
                        }

                        Button(onClick = { screen = Screen.SopDetail }) { Text("Back") }
                    }

                    Screen.Settings -> {
                        Text("Settings", style = MaterialTheme.typography.headlineMedium)
                        Text("Signed in user: $signedInUser")
                        OutlinedTextField(
                            value = convexUrl,
                            onValueChange = { convexUrl = it.trim() },
                            label = { Text("Convex URL") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(onClick = {
                            scope.launch {
                                loading = true
                                statusMessage = try {
                                    val user = repository.observeCurrentUserEmail().first() ?: "Connected (no user email)"
                                    "Connected: $user"
                                } catch (error: Throwable) {
                                    "Connection failed: ${error.message}"
                                }
                                loading = false
                            }
                        }) {
                            Text("Test Backend")
                        }
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
