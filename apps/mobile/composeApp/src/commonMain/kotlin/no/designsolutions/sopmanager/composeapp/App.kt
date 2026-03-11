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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
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
import no.designsolutions.sopmanager.composeapp.logging.AppLogger
import no.designsolutions.sopmanager.composeapp.qr.QrParser
import no.designsolutions.sopmanager.composeapp.qr.provideQrScanner
import no.designsolutions.sopmanager.composeapp.repository.SopRepository
import no.designsolutions.sopmanager.composeapp.ui.components.AppTopBar
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
@OptIn(ExperimentalComposeUiApi::class)
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
        LaunchedEffect(Unit) {
            AppLogger.init()
            AppLogger.i("App composed")
        }

        fun navigateTo(route: AppRoute) {
            backStack.add(route)
            AppLogger.d("Navigate -> $route (stack=${backStack.size})")
        }

        fun goBack() {
            if (backStack.size > 1) {
                val popped = backStack.removeLastOrNull()
                AppLogger.d("Back <- $popped (stack=${backStack.size})")
            }
        }

        fun resetTo(route: AppRoute) {
            backStack.clear()
            backStack.add(route)
            AppLogger.i("Navigation reset -> $route")
        }

        val currentRoute = (backStack.lastOrNull() as? AppRoute) ?: AppRoute.SignIn
        val showTopBar = currentRoute != AppRoute.SignIn
        fun routeTitle(route: AppRoute): String =
            when (route) {
                AppRoute.SignIn -> "SOP Manager"
                AppRoute.Home -> "Home"
                AppRoute.Search -> "Search"
                AppRoute.SopDetail -> "SOP Detail"
                AppRoute.Edit -> "Edit SOP"
                AppRoute.History -> "History"
                AppRoute.Settings -> "Settings"
            }
        val topBarTitle = routeTitle(currentRoute)
        val previousRoute = backStack.getOrNull(backStack.lastIndex - 1) as? AppRoute
        val backLabel = previousRoute?.let(::routeTitle)

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
                            AppLogger.d("QR scan requested")
                            qrScanner.scan(
                                onSuccess = { payload ->
                                    val parsed = QrParser.extractPartNumber(payload)
                                    if (parsed == null) {
                                        AppLogger.w("QR payload did not contain valid part number")
                                        vm.updateStatusMessage("Invalid QR code")
                                    } else {
                                        AppLogger.i("QR parsed part number: $parsed")
                                        vm.loadProcedureByPart(parsed) { navigateTo(AppRoute.SopDetail) }
                                    }
                                },
                                onError = { err ->
                                    AppLogger.e("QR scan failed: $err")
                                    vm.updateStatusMessage(err)
                                },
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
                    )
                }

                entry(AppRoute.Edit) {
                    EditSopScreen(
                        title = vm.sopTitle,
                        body = vm.sopBody,
                        steps = vm.editSteps,
                        onTitleChange = vm::updateSopTitle,
                        onBodyChange = vm::updateSopBody,
                        onStepDescriptionChange = vm::updateStepDescription,
                        onAddStep = vm::addStep,
                        onRemoveStep = vm::removeStep,
                        onMoveStep = vm::moveStep,
                        onAttachMedia = { vm.updateStatusMessage("Media picker integration pending on this build.") },
                        onCaptureMedia = { vm.updateStatusMessage("Camera capture integration pending on this build.") },
                        onSave = { vm.saveSop(::goBack) },
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

        BackHandler(enabled = backStack.size > 1) {
            AppLogger.d("System back pressed")
            goBack()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showTopBar) {
                    AppTopBar(
                        title = topBarTitle,
                        canNavigateBack = backStack.size > 1,
                        backLabel = backLabel,
                        onBack = ::goBack,
                    )
                }
            },
        ) { padding ->
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
