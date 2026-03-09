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
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import no.designsolutions.sopmanager.composeapp.data.Convex
import no.designsolutions.sopmanager.composeapp.data.repository.ConvexSopRepository
import no.designsolutions.sopmanager.composeapp.qr.QrParser
import no.designsolutions.sopmanager.composeapp.qr.provideQrScanner
import no.designsolutions.sopmanager.composeapp.repository.SopRepository
import no.designsolutions.sopmanager.composeapp.ui.screens.EditSopScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.HistoryScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.HomeScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SearchScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SettingsScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SignInScreen
import no.designsolutions.sopmanager.composeapp.ui.screens.SopDetailScreen
import no.designsolutions.sopmanager.composeapp.ui.theme.AppTheme

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
        val qrScanner = retain { provideQrScanner() }
        val vm = viewModel { AppViewModel(repository) }

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

        val activeEntries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
            entryProvider = entryProvider {
                entry(AppRoute.SignIn) {
                    SignInScreen(onContinue = { vm.signInMock { resetTo(AppRoute.Home) } })
                }

                entry(AppRoute.Home) {
                    HomeScreen(
                        onScanQr = {
                            qrScanner.scan(
                                onSuccess = { payload ->
                                    val parsed = QrParser.extractPartNumber(payload)
                                    if (parsed == null) {
                                        vm.updateStatusMessage("Invalid QR code")
                                    } else {
                                        vm.loadProcedureByPart(parsed) { navigateTo(AppRoute.SopDetail) }
                                    }
                                },
                                onError = { err -> vm.updateStatusMessage(err) },
                            )
                        },
                        onSearch = { navigateTo(AppRoute.Search) },
                        onSettings = { navigateTo(AppRoute.Settings) },
                    )
                }

                entry(AppRoute.Search) {
                    SearchScreen(
                        partNumber = vm.partNumber,
                        onPartNumberChange = vm::updatePartNumber,
                        results = vm.searchResults,
                        onSearch = vm::searchParts,
                        onBack = ::goBack,
                        onSelectResult = { result ->
                            vm.loadProcedureByPart(result.partNumber) { navigateTo(AppRoute.SopDetail) }
                        },
                    )
                }

                entry(AppRoute.SopDetail) {
                    SopDetailScreen(
                        partNumber = vm.partNumber,
                        detail = vm.currentDetail,
                        onCreate = {
                            vm.prepareCreateSop()
                            navigateTo(AppRoute.Edit)
                        },
                        onEdit = {
                            vm.prepareEditSopFromLatest()
                            navigateTo(AppRoute.Edit)
                        },
                        onHistory = { navigateTo(AppRoute.History) },
                        onBack = ::goBack,
                    )
                }

                entry(AppRoute.Edit) {
                    EditSopScreen(
                        title = vm.sopTitle,
                        body = vm.sopBody,
                        photoDescriptions = vm.editPhotoDescriptions,
                        onTitleChange = vm::updateSopTitle,
                        onBodyChange = vm::updateSopBody,
                        onPhotoDescriptionChange = vm::updatePhotoDescription,
                        onSave = { vm.saveSop(::goBack) },
                        onCancel = ::goBack,
                    )
                }

                entry(AppRoute.History) {
                    LaunchedEffect(vm.currentDetail?.procedureId) {
                        vm.observeHistoryForCurrentProcedure()
                    }

                    HistoryScreen(
                        versions = vm.versionList,
                        selectedVersion = vm.selectedVersion,
                        onSelectVersion = vm::selectVersion,
                        onBack = ::goBack,
                    )
                }

                entry(AppRoute.Settings) {
                    SettingsScreen(
                        signedInUser = vm.signedInUser,
                        onTestBackend = vm::testBackend,
                        onSignOut = { vm.signOut { resetTo(AppRoute.SignIn) } },
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
                if (vm.loading) {
                    CircularProgressIndicator()
                }

                activeEntries.lastOrNull()?.Content()

                vm.statusMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
