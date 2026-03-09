package no.designsolutions.sopmanager.composeapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import no.designsolutions.sopmanager.composeapp.data.Convex
import no.designsolutions.sopmanager.composeapp.data.repository.ConvexSopRepository
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.SopVersion
import no.designsolutions.sopmanager.composeapp.qr.QrParser
import no.designsolutions.sopmanager.composeapp.qr.provideQrScanner
import no.designsolutions.sopmanager.composeapp.repository.PhotoPayload
import no.designsolutions.sopmanager.composeapp.repository.SopRepository
import no.designsolutions.sopmanager.composeapp.ui.screens.EditSopScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.HistoryScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.HomeScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SearchScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SettingsScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SignInScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SopDetailScreen
import no.designsolutions.sopmanager.composeapp.ui.theme.AppTheme
import androidx.savedstate.serialization.SavedStateConfiguration

@Serializable
private sealed interface AppRoute : NavKey {
    @Serializable data object SignIn : AppRoute
    @Serializable data object Home : AppRoute
    @Serializable data object Search : AppRoute
    @Serializable data object SopDetail : AppRoute
    @Serializable data object Edit : AppRoute
    @Serializable data object History : AppRoute
    @Serializable data object Settings : AppRoute
}

@Composable
fun App() {
    AppTheme(isSystemInDarkTheme()) {
        val backStackConfiguration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.SignIn::class)
                    subclass(AppRoute.Home::class)
                    subclass(AppRoute.Search::class)
                    subclass(AppRoute.SopDetail::class)
                    subclass(AppRoute.Edit::class)
                    subclass(AppRoute.History::class)
                    subclass(AppRoute.Settings::class)
                }
            }
        }
        val backStack = rememberNavBackStack(backStackConfiguration, AppRoute.SignIn)
        val repository: SopRepository = retain { ConvexSopRepository(Convex.client) }

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

        fun navigateTo(route: AppRoute) {
            backStack.add(route)
        }

        fun goBack() {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }

        fun resetTo(route: AppRoute) {
            backStack.clear()
            backStack.add(route)
        }

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

        val activeEntries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
            entryProvider = entryProvider {
                entry(AppRoute.SignIn) {
                    SignInScreen(
                        onContinue = {
                            scope.launch {
                                loading = true
                                signedInUser = try {
                                    repository.observeCurrentUserEmail().first() ?: "mock-user@local"
                                } catch (_: Throwable) {
                                    "mock-user@local"
                                }
                                loading = false
                                statusMessage = "Signed in (mocked auth)"
                                resetTo(AppRoute.Home)
                            }
                        },
                    )
                }

                entry(AppRoute.Home) {
                    HomeScreen(
                        onScanQr = {
                            qrScanner.scan(
                                onSuccess = { payload ->
                                    val parsed = QrParser.extractPartNumber(payload)
                                    if (parsed == null) {
                                        statusMessage = "Invalid QR code"
                                    } else {
                                        loadProcedureByPart(parsed) { navigateTo(AppRoute.SopDetail) }
                                    }
                                },
                                onError = { err -> statusMessage = err },
                            )
                        },
                        onSearch = { navigateTo(AppRoute.Search) },
                        onSettings = { navigateTo(AppRoute.Settings) },
                    )
                }

                entry(AppRoute.Search) {
                    SearchScreen(
                        partNumber = partNumber,
                        onPartNumberChange = { partNumber = it },
                        results = searchResults,
                        onSearch = {
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
                        },
                        onBack = { goBack() },
                        onSelectResult = { result ->
                            loadProcedureByPart(result.partNumber) { navigateTo(AppRoute.SopDetail) }
                        },
                    )
                }

                entry(AppRoute.SopDetail) {
                    SopDetailScreen(
                        partNumber = partNumber,
                        detail = currentDetail,
                        onCreate = {
                            sopTitle = ""
                            sopBody = ""
                            editPhotoDescriptions.clear()
                            navigateTo(AppRoute.Edit)
                        },
                        onEdit = {
                            val latest = currentDetail?.latest ?: return@SopDetailScreen
                            sopTitle = latest.title
                            sopBody = latest.body
                            editPhotoDescriptions.clear()
                            editPhotoDescriptions.addAll(latest.photos.map { it.description.orEmpty() })
                            navigateTo(AppRoute.Edit)
                        },
                        onHistory = { navigateTo(AppRoute.History) },
                        onBack = { goBack() },
                    )
                }

                entry(AppRoute.Edit) {
                    EditSopScreen(
                        title = sopTitle,
                        body = sopBody,
                        photoDescriptions = editPhotoDescriptions,
                        onTitleChange = { sopTitle = it },
                        onBodyChange = { sopBody = it },
                        onPhotoDescriptionChange = { index, value ->
                            editPhotoDescriptions[index] = value
                        },
                        onSave = {
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
                                    loadProcedureByPart(partNumber) { goBack() }
                                } catch (error: Throwable) {
                                    statusMessage = error.message ?: "Save failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        onCancel = { goBack() },
                    )
                }

                entry(AppRoute.History) {
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

                    HistoryScreen(
                        versions = versionList,
                        selectedVersion = selectedVersion,
                        onSelectVersion = { version ->
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
                        onBack = { goBack() },
                    )
                }

                entry(AppRoute.Settings) {
                    SettingsScreen(
                        signedInUser = signedInUser,
                        onTestBackend = {
                            scope.launch {
                                loading = true
                                statusMessage = try {
                                    val user = repository.observeCurrentUserEmail().first()
                                        ?: "Connected (no user email)"
                                    "Connected: $user"
                                } catch (error: Throwable) {
                                    "Connection failed: ${error.message}"
                                }
                                loading = false
                            }
                        },
                        onSignOut = {
                            statusMessage = "Signed out"
                            resetTo(AppRoute.SignIn)
                        },
                    )
                }
            },
        )

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

                activeEntries.lastOrNull()?.Content()

                statusMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
