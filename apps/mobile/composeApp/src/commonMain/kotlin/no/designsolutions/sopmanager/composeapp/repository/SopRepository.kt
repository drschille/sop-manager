package no.designsolutions.sopmanager.composeapp.repository

import kotlinx.coroutines.flow.Flow
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.SopVersion

interface SopRepository {
    fun observeByPartNumber(partNumber: String): Flow<ProcedureDetail?>
    fun observeSearchParts(query: String): Flow<List<PartSearchResult>>
    fun observeVersions(procedureId: String): Flow<List<SopVersion>>
    fun observeVersion(versionId: String): Flow<SopVersion?>
    fun observeCurrentUserEmail(): Flow<String?>

    suspend fun getByPartNumber(partNumber: String): ProcedureDetail?
    suspend fun searchParts(query: String): List<PartSearchResult>
    suspend fun listVersions(procedureId: String): List<SopVersion>
    suspend fun getVersion(versionId: String): SopVersion?
    suspend fun saveNew(partNumber: String, title: String, body: String, photos: List<PhotoPayload>)
    suspend fun saveEdit(procedureId: String, title: String, body: String, photos: List<PhotoPayload>)
    suspend fun currentUserEmail(): String?
}

data class PhotoPayload(
    val storageId: String,
    val description: String? = null,
)
