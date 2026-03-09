package no.designsolutions.sopmanager.composeapp.data.repository

import com.kansson.kmp.convex.core.ConvexClient
import com.kansson.kmp.convex.core.ConvexResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import no.designsolutions.sopmanager.composeapp.data.Convex
import no.designsolutions.sopmanager.composeapp.data.api.SavePhoto
import no.designsolutions.sopmanager.composeapp.data.api.SopApi
import no.designsolutions.sopmanager.composeapp.data.api.toSopVersion
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail
import no.designsolutions.sopmanager.composeapp.model.SopVersion
import no.designsolutions.sopmanager.composeapp.repository.PhotoPayload
import no.designsolutions.sopmanager.composeapp.repository.SopRepository

class ConvexSopRepository(
    val client: ConvexClient = Convex.client,
) : SopRepository {
  override fun observeByPartNumber(partNumber: String): Flow<ProcedureDetail?> {
    return client.query(SopApi.Procedures.GetByPartNumber(partNumber = partNumber)).mapSuccess {
        response ->
      val part = response.part ?: return@mapSuccess null
      ProcedureDetail(
          procedureId = response.procedure?.id,
          partNumber = part.partNumber,
          latest = response.currentVersion?.toSopVersion(),
      )
    }
  }

  override fun observeSearchParts(query: String): Flow<List<PartSearchResult>> {
    return client.query(SopApi.Parts.Search(query = query)).mapSuccess { results ->
      results.map {
        PartSearchResult(
            partNumber = it.partNumber,
            sopTitle = it.sopTitle,
            thumbnailUrl = it.thumbnailUrl,
        )
      }
    }
  }

  override fun observeVersions(procedureId: String): Flow<List<SopVersion>> {
    return client.query(SopApi.Procedures.ListVersions(procedureId = procedureId)).mapSuccess { versions ->
      versions.map {
        SopVersion(
            id = it.id,
            versionNumber = it.versionNumber,
            title = it.title,
            body = "",
            photos = emptyList(),
            createdAt = it.createdAt.toString(),
            createdBy = it.createdBy,
        )
      }
    }
  }

  override fun observeVersion(versionId: String): Flow<SopVersion?> {
    return client.query(SopApi.Procedures.GetVersion(versionId = versionId)).mapSuccess { it?.toSopVersion() }
  }

  override fun observeCurrentUserEmail(): Flow<String?> {
    return client.query(SopApi.Auth.GetCurrentUser()).mapSuccess { it.email ?: it.name }
  }

  override suspend fun getByPartNumber(partNumber: String): ProcedureDetail? {
    return observeByPartNumber(partNumber).first()
  }

  override suspend fun searchParts(query: String): List<PartSearchResult> {
    return observeSearchParts(query).first()
  }

  override suspend fun listVersions(procedureId: String): List<SopVersion> {
    return observeVersions(procedureId).first()
  }

  override suspend fun getVersion(versionId: String): SopVersion? {
    return observeVersion(versionId).first()
  }

  override suspend fun saveNew(
      partNumber: String,
      title: String,
      body: String,
      photos: List<PhotoPayload>,
  ) {
    client.mutation(
            SopApi.Procedures.Create(
                partNumber = partNumber,
                title = title,
                body = body,
                photos = photos.map { SavePhoto(it.storageId, it.description) },
            ),
        )
        .requireSuccess()
  }

  override suspend fun saveEdit(
      procedureId: String,
      title: String,
      body: String,
      photos: List<PhotoPayload>,
  ) {
    client.mutation(
            SopApi.Procedures.Edit(
                procedureId = procedureId,
                title = title,
                body = body,
                photos = photos.map { SavePhoto(it.storageId, it.description) },
            ),
        )
        .requireSuccess()
  }

  override suspend fun currentUserEmail(): String? {
    return observeCurrentUserEmail().first()
  }
}

fun <I, O> Flow<ConvexResponse<I>>.mapSuccess(mapper: (I) -> O): Flow<O> {
  return map { response ->
    when (response) {
      is ConvexResponse.Success -> mapper(response.data)
      is ConvexResponse.Failure -> throw IllegalStateException(response.message)
    }
  }
}

fun ConvexResponse<*>.requireSuccess() {
  if (this is ConvexResponse.Failure) {
    throw IllegalStateException(message)
  }
}

