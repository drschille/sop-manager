package no.designsolutions.sopmanager.composeapp.repository

import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.SopVersion

interface SopRepository {
    suspend fun getByPartNumber(partNumber: String): ProcedureDetail?
    suspend fun searchParts(query: String): List<String>
    suspend fun listVersions(procedureId: String): List<SopVersion>
    suspend fun saveNew(partNumber: String, title: String, body: String, photoIds: List<String>)
    suspend fun saveEdit(procedureId: String, title: String, body: String, photoIds: List<String>)
}
