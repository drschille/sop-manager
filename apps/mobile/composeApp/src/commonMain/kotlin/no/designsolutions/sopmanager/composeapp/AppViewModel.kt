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
    val editPhotoDescriptions = mutableStateListOf<String>()

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
        statusMessage = message
    }

    fun updatePartNumber(value: String) {
        partNumber = value
    }

    fun updateSopTitle(value: String) {
        sopTitle = value
    }

    fun updateSopBody(value: String) {
        sopBody = value
    }

    fun updatePhotoDescription(index: Int, value: String) {
        if (index in editPhotoDescriptions.indices) {
            editPhotoDescriptions[index] = value
        }
    }

    fun signInMock(onSignedIn: () -> Unit) {
        viewModelScope.launch {
            loading = true
            signedInUser = try {
                repository.observeCurrentUserEmail().first() ?: "mock-user@local"
            } catch (_: Throwable) {
                "mock-user@local"
            }
            loading = false
            statusMessage = "Signed in (mocked auth)"
            onSignedIn()
        }
    }

    fun searchParts() {
        searchSubscription?.cancel()
        searchSubscription = viewModelScope.launch {
            loading = true
            statusMessage = null
            try {
                repository.observeSearchParts(partNumber).collect { results ->
                    searchResults.clear()
                    searchResults.addAll(results)
                    loading = false
                }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Search failed"
                loading = false
            }
        }
    }

    fun loadProcedureByPart(targetPartNumber: String, onLoaded: (() -> Unit)? = null) {
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

    fun prepareCreateSop() {
        sopTitle = ""
        sopBody = ""
        editPhotoDescriptions.clear()
    }

    fun prepareEditSopFromLatest() {
        val latest = currentDetail?.latest ?: return
        sopTitle = latest.title
        sopBody = latest.body
        editPhotoDescriptions.clear()
        editPhotoDescriptions.addAll(latest.photos.map { it.description.orEmpty() })
    }

    fun saveSop(onSaved: () -> Unit) {
        viewModelScope.launch {
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
                loadProcedureByPart(partNumber) { onSaved() }
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Save failed"
                loading = false
            }
        }
    }

    fun observeHistoryForCurrentProcedure() {
        val procedureId = currentDetail?.procedureId ?: return
        if (procedureId == observedHistoryProcedureId && historySubscription?.isActive == true) {
            return
        }
        observedHistoryProcedureId = procedureId
        historySubscription?.cancel()
        historySubscription = viewModelScope.launch {
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
    }

    fun selectVersion(version: SopVersion) {
        viewModelScope.launch {
            loading = true
            selectedVersion = try {
                repository.observeVersion(version.id).first()
            } catch (error: Throwable) {
                statusMessage = error.message ?: "Failed to load version"
                null
            }
            loading = false
        }
    }

    fun testBackend() {
        viewModelScope.launch {
            loading = true
            statusMessage = try {
                val user = repository.observeCurrentUserEmail().first() ?: "Connected (no user email)"
                "Connected: $user"
            } catch (error: Throwable) {
                "Connection failed: ${error.message}"
            }
            loading = false
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        statusMessage = "Signed out"
        onSignedOut()
    }

    override fun onCleared() {
        detailSubscription?.cancel()
        searchSubscription?.cancel()
        historySubscription?.cancel()
        super.onCleared()
    }
}
