package no.designsolutions.sopmanager.composeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import no.designsolutions.sopmanager.composeapp.logging.AppLogger
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.SopVersion
import no.designsolutions.sopmanager.composeapp.repository.PhotoPayload
import no.designsolutions.sopmanager.composeapp.repository.SopRepository

class AppViewModel(
    private val repository: SopRepository,
) : ViewModel() {
    var partNumber by mutableStateOf("")
        private set
    var currentDetail by mutableStateOf<ProcedureDetail?>(null)
        private set
    var sopTitle by mutableStateOf("")
        private set
    var sopBody by mutableStateOf("")
        private set
    var selectedVersion by mutableStateOf<SopVersion?>(null)
        private set

    val versionList = mutableStateListOf<SopVersion>()
    val searchResults = mutableStateListOf<PartSearchResult>()
    val editSteps = mutableStateListOf<StepDraft>()

    var signedInUser by mutableStateOf("mock-user@local")
        private set
    var loading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set

    private var detailSubscription: Job? = null
    private var searchSubscription: Job? = null
    private var historySubscription: Job? = null
    private var observedHistoryProcedureId: String? = null

    fun updateStatusMessage(message: String?) {
        AppLogger.d("Status message updated: ${message ?: "<cleared>"}")
        statusMessage = message
    }

    fun updatePartNumber(value: String) {
        AppLogger.d("Part number changed: $value")
        partNumber = value
    }

    fun updateSopTitle(value: String) {
        sopTitle = value
    }

    fun updateSopBody(value: String) {
        sopBody = value
    }

    fun addStep() {
        editSteps.add(StepDraft())
    }

    fun removeStep(index: Int) {
        if (index in editSteps.indices) {
            editSteps.removeAt(index)
        }
    }

    fun moveStep(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in editSteps.indices || toIndex !in editSteps.indices || fromIndex == toIndex) return
        val step = editSteps.removeAt(fromIndex)
        editSteps.add(toIndex, step)
    }

    fun updateStepDescription(index: Int, value: String) {
        if (index in editSteps.indices) {
            editSteps[index] = editSteps[index].copy(description = value)
        }
    }

    fun addStepMedia(index: Int, media: StepMedia) {
        if (index !in editSteps.indices) return
        val updated = editSteps[index].media + media
        editSteps[index] = editSteps[index].copy(media = updated)
    }

    fun removeStepMedia(stepIndex: Int, mediaIndex: Int) {
        if (stepIndex !in editSteps.indices) return
        val current = editSteps[stepIndex]
        if (mediaIndex !in current.media.indices) return
        val updated = current.media.toMutableList().apply { removeAt(mediaIndex) }
        editSteps[stepIndex] = current.copy(media = updated)
    }

    fun signInMock(onSignedIn: () -> Unit) {
        AppLogger.i("Mock sign-in started")
        viewModelScope.launch {
            loading = true
            signedInUser = try {
                repository.observeCurrentUserEmail().first() ?: "mock-user@local"
            } catch (_: Throwable) {
                "mock-user@local"
            }
            loading = false
            statusMessage = "Signed in (mocked auth)"
            AppLogger.i("Mock sign-in completed as $signedInUser")
            onSignedIn()
        }
    }

    fun searchParts() {
        AppLogger.i("Search started for query: $partNumber")
        searchSubscription?.cancel()
        searchSubscription = viewModelScope.launch {
            loading = true
            statusMessage = null
            try {
                repository.observeSearchParts(partNumber).collect { results ->
                    searchResults.clear()
                    searchResults.addAll(results)
                    loading = false
                    AppLogger.d("Search returned ${results.size} result(s)")
                }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Search failed"
                loading = false
                AppLogger.e("Search failed for query: $partNumber", error)
            }
        }
    }

    fun loadProcedureByPart(targetPartNumber: String, onLoaded: (() -> Unit)? = null) {
        AppLogger.i("Loading procedure for part: ${targetPartNumber.trim()}")
        detailSubscription?.cancel()
        detailSubscription = viewModelScope.launch {
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
                    AppLogger.d(
                        "Procedure stream update for $partNumber: " +
                            "procedureId=${detail?.procedureId ?: "none"}, hasLatest=${latest != null}"
                    )
                    if (!opened) {
                        onLoaded?.invoke()
                        opened = true
                    }
                    loading = false
                }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Failed to load SOP"
                loading = false
                AppLogger.e("Failed to load procedure for part: $partNumber", error)
            }
        }
    }

    fun prepareCreateSop() {
        AppLogger.d("Preparing create SOP state for part: $partNumber")
        sopTitle = ""
        sopBody = ""
        editSteps.clear()
        editSteps.add(StepDraft())
    }

    fun prepareEditSopFromLatest() {
        val latest = currentDetail?.latest ?: return
        AppLogger.d("Preparing edit state from latest SOP version: ${latest.versionNumber}")
        sopTitle = latest.title
        sopBody = latest.body
        editSteps.clear()
        if (latest.photos.isEmpty()) {
            editSteps.add(StepDraft(description = latest.body))
        } else {
            editSteps.addAll(latest.photos.map { StepDraft(description = it.description.orEmpty()) })
        }
    }

    fun saveSop(onSaved: () -> Unit) {
        AppLogger.i("Saving SOP for part: $partNumber")
        viewModelScope.launch {
            loading = true
            statusMessage = null
            try {
                val photos = editSteps
                    .flatMap { it.media }
                    .filter { it.storageId.isNotBlank() }
                    .map { PhotoPayload(storageId = it.storageId, description = it.label) }

                val procedureId = currentDetail?.procedureId
                if (procedureId.isNullOrBlank()) {
                    repository.saveNew(partNumber, sopTitle, sopBody, photos)
                    AppLogger.i("Created new SOP for part: $partNumber")
                } else {
                    repository.saveEdit(procedureId, sopTitle, sopBody, photos)
                    AppLogger.i("Saved SOP edit for procedure: $procedureId")
                }
                statusMessage = "SOP saved"
                loadProcedureByPart(partNumber) { onSaved() }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Save failed"
                loading = false
                AppLogger.e("Failed to save SOP for part: $partNumber", error)
            }
        }
    }

    fun observeHistoryForCurrentProcedure() {
        val procedureId = currentDetail?.procedureId ?: return
        if (procedureId == observedHistoryProcedureId && historySubscription?.isActive == true) {
            return
        }
        AppLogger.i("Observing version history for procedure: $procedureId")
        observedHistoryProcedureId = procedureId
        historySubscription?.cancel()
        historySubscription = viewModelScope.launch {
            loading = true
            try {
                repository.observeVersions(procedureId).collect { versions ->
                    versionList.clear()
                    versionList.addAll(versions)
                    loading = false
                    AppLogger.d("History stream update: ${versions.size} version(s)")
                }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Failed to load versions"
                loading = false
                AppLogger.e("Failed to load version history for procedure: $procedureId", error)
            }
        }
    }

    fun selectVersion(version: SopVersion) {
        AppLogger.d("Loading selected version id=${version.id}, number=${version.versionNumber}")
        viewModelScope.launch {
            loading = true
            selectedVersion = try {
                repository.observeVersion(version.id).first()
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Failed to load version"
                AppLogger.e("Failed to load selected version id=${version.id}", error)
                null
            }
            loading = false
            AppLogger.d("Selected version loaded: ${selectedVersion?.versionNumber ?: "none"}")
        }
    }

    fun testBackend() {
        AppLogger.i("Testing backend connectivity")
        viewModelScope.launch {
            loading = true
            statusMessage = try {
                val user = repository.observeCurrentUserEmail().first() ?: "Connected (no user email)"
                "Connected: $user"
            } catch (error: Throwable) {
                AppLogger.e("Backend connectivity test failed", error)
                "Connection failed: ${error.message}"
            }
            loading = false
            AppLogger.i("Backend connectivity test result: $statusMessage")
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        AppLogger.i("Sign-out requested")
        statusMessage = "Signed out"
        onSignedOut()
    }

    override fun onCleared() {
        AppLogger.i("AppViewModel clearing; cancelling subscriptions")
        detailSubscription?.cancel()
        searchSubscription?.cancel()
        historySubscription?.cancel()
        super.onCleared()
    }
}


data class StepDraft(
    val description: String = "",
    val media: List<StepMedia> = emptyList(),
)

enum class StepMediaType { Image, Video }

data class StepMedia(
    val uri: String,
    val type: StepMediaType,
    val label: String? = null,
    val storageId: String = "",
)
