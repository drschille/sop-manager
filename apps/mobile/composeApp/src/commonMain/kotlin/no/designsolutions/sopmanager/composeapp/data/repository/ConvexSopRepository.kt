package no.designsolutions.sopmanager.composeapp.data.repository

import com.kansson.kmp.convex.core.ConvexClient
import com.kansson.kmp.convex.core.ConvexFunction
import com.kansson.kmp.convex.core.ConvexResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.designsolutions.sopmanager.composeapp.data.Convex
import no.designsolutions.sopmanager.composeapp.data.api.SopApi
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult
import no.designsolutions.sopmanager.composeapp.model.PhotoRef
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
    val function =
        object : ConvexFunction.Query<SearchArgs, List<SearchItemResponse>> {
          override val identifier = "parts:search"
          override val args = SearchArgs(query)
        }
    return client.query(function).mapSuccess { results ->
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
    val function =
        object : ConvexFunction.Query<ProcedureIdArgs, List<VersionListItemResponse>> {
          override val identifier = "procedures:listVersions"
          override val args = ProcedureIdArgs(procedureId)
        }
    return client.query(function).mapSuccess { versions ->
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
    val function =
        object : ConvexFunction.Query<VersionIdArgs, VersionResponse?> {
          override val identifier = "procedures:getVersion"
          override val args = VersionIdArgs(versionId)
        }
    return client.query(function).mapSuccess { it?.toSopVersion() }
  }

  override fun observeCurrentUserEmail(): Flow<String?> {
    val function =
        object : ConvexFunction.Query<EmptyArgs, CurrentUserResponse> {
          override val identifier = "auth:getCurrentUser"
          override val args = EmptyArgs()
        }
    return client.query(function).mapSuccess { it.email ?: it.name }
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
    val function =
        object : ConvexFunction.Mutation<CreateArgs, MutationResult> {
          override val identifier = "procedures:create"
          override val args =
              CreateArgs(
                  partNumber = partNumber,
                  title = title,
                  body = body,
                  photos = photos.map { SavePhoto(it.storageId, it.description) },
              )
        }
    client.mutation(function).requireSuccess()
  }

  override suspend fun saveEdit(
      procedureId: String,
      title: String,
      body: String,
      photos: List<PhotoPayload>,
  ) {
    val function =
        object : ConvexFunction.Mutation<EditArgs, MutationResult> {
          override val identifier = "procedures:edit"
          override val args =
              EditArgs(
                  procedureId = procedureId,
                  title = title,
                  body = body,
                  photos = photos.map { SavePhoto(it.storageId, it.description) },
              )
        }
    client.mutation(function).requireSuccess()
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

@Serializable data class EmptyArgs(val placeholder: String? = null)

@Serializable data class SearchArgs(val partNumberQuery: String)

@Serializable data class ProcedureIdArgs(val procedureId: String)

@Serializable data class VersionIdArgs(val versionId: String)

@Serializable
data class CreateArgs(
    val partNumber: String,
    val title: String,
    val body: String,
    val photos: List<SavePhoto>,
)

@Serializable
data class EditArgs(
    val procedureId: String,
    val title: String,
    val body: String,
    val photos: List<SavePhoto>,
)

@Serializable
data class MutationResult(
    @SerialName("versionId") val versionId: String? = null,
)

@Serializable
data class SavePhoto(
    val storageId: String,
    val description: String? = null,
)

@Serializable
data class SearchItemResponse(
    val partNumber: String,
    val sopTitle: String? = null,
    val thumbnailUrl: String? = null,
)

@Serializable data class PartResponse(val partNumber: String)

@Serializable data class ProcedureResponse(@SerialName("_id") val id: String)

@Serializable
data class VersionListItemResponse(
    @SerialName("_id") val id: String,
    val versionNumber: Int,
    val createdAt: Long,
    val createdBy: String,
    val title: String,
)

@Serializable
data class VersionResponse(
    @SerialName("_id") val id: String,
    val versionNumber: Int,
    val title: String,
    val body: String,
    val createdAt: Long,
    val createdBy: String,
    val photos: List<PhotoResponse> = emptyList(),
)

@Serializable
data class PhotoResponse(
    val storageId: String,
    val url: String? = null,
    val description: String? = null,
)

@Serializable
data class CurrentUserResponse(
    val email: String? = null,
    val name: String? = null,
)

fun VersionResponse.toSopVersion(): SopVersion {
  return SopVersion(
      id = id,
      versionNumber = versionNumber,
      title = title,
      body = body,
      photos =
          photos.map {
            PhotoRef(
                storageId = it.storageId,
                previewUrl = it.url,
                description = it.description,
            )
          },
      createdAt = createdAt.toString(),
      createdBy = createdBy,
  )
}
