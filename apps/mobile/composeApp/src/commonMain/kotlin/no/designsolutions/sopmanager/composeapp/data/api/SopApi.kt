package no.designsolutions.sopmanager.composeapp.data.api

import com.kansson.kmp.convex.core.ConvexFunction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import no.designsolutions.sopmanager.composeapp.model.PhotoRef
import no.designsolutions.sopmanager.composeapp.model.SopVersion

object SopApi {
  object Parts {
    data class Search(
        override val identifier: String = "parts:search",
        override val args: SearchArgs,
    ) : ConvexFunction.Query<SearchArgs, List<SearchItemResponse>> {
      constructor(query: String) : this(args = SearchArgs(query))
    }
  }

  object Procedures {
    data class GetByPartNumber(
        override val identifier: String = "procedures:getByPartNumber",
        override val args: ByPartArgs,
    ) : ConvexFunction.Query<ByPartArgs, ProcedureByPartResponse> {
      constructor(partNumber: String) : this(args = ByPartArgs(partNumber))
    }

    data class ListVersions(
        override val identifier: String = "procedures:listVersions",
        override val args: ProcedureIdArgs,
    ) : ConvexFunction.Query<ProcedureIdArgs, List<VersionListItemResponse>> {
      constructor(procedureId: String) : this(args = ProcedureIdArgs(procedureId))
    }

    data class GetVersion(
        override val identifier: String = "procedures:getVersion",
        override val args: VersionIdArgs,
    ) : ConvexFunction.Query<VersionIdArgs, VersionResponse?> {
      constructor(versionId: String) : this(args = VersionIdArgs(versionId))
    }

    data class Create(
        override val identifier: String = "procedures:create",
        override val args: CreateArgs,
    ) : ConvexFunction.Mutation<CreateArgs, MutationResult> {
      constructor(
          partNumber: String,
          title: String,
          body: String,
          photos: List<SavePhoto>,
      ) : this(
          args =
              CreateArgs(
                  partNumber = partNumber,
                  title = title,
                  body = body,
                  photos = photos,
              ),
      )
    }

    data class Edit(
        override val identifier: String = "procedures:edit",
        override val args: EditArgs,
    ) : ConvexFunction.Mutation<EditArgs, MutationResult> {
      constructor(
          procedureId: String,
          title: String,
          body: String,
          photos: List<SavePhoto>,
      ) : this(
          args =
              EditArgs(
                  procedureId = procedureId,
                  title = title,
                  body = body,
                  photos = photos,
              ),
      )
    }
  }

  object Auth {
    data class GetCurrentUser(
        override val identifier: String = "auth:getCurrentUser",
        override val args: EmptyArgs = EmptyArgs(),
    ) : ConvexFunction.Query<EmptyArgs, CurrentUserResponse>
  }
}

@Serializable data class ByPartArgs(val partNumber: String)

@Serializable
data class ProcedureByPartResponse(
    val part: PartResponse? = null,
    val procedure: ProcedureResponse? = null,
    val currentVersion: VersionResponse? = null,
)

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
    @Serializable(with = FlexibleIntSerializer::class)
    val versionNumber: Int,
    @Serializable(with = FlexibleLongSerializer::class)
    val createdAt: Long,
    val createdBy: String,
    val title: String,
)

@Serializable
data class VersionResponse(
    @SerialName("_id") val id: String,
    @Serializable(with = FlexibleIntSerializer::class)
    val versionNumber: Int,
    val title: String,
    val body: String,
    @Serializable(with = FlexibleLongSerializer::class)
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

object FlexibleLongSerializer : KSerializer<Long> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("FlexibleLong", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Long) {
    encoder.encodeLong(value)
  }

  override fun deserialize(decoder: Decoder): Long {
    val jsonPrimitive = (decoder as? JsonDecoder)?.decodeJsonElement() as? JsonPrimitive
    if (jsonPrimitive != null) {
      jsonPrimitive.longOrNull?.let { return it }
      jsonPrimitive.doubleOrNull?.let { return it.toLong() }
      jsonPrimitive.contentOrNull?.toDoubleOrNull()?.let { return it.toLong() }
    }
    return decoder.decodeLong()
  }
}

object FlexibleIntSerializer : KSerializer<Int> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: Int) {
    encoder.encodeInt(value)
  }

  override fun deserialize(decoder: Decoder): Int {
    val jsonPrimitive = (decoder as? JsonDecoder)?.decodeJsonElement() as? JsonPrimitive
    if (jsonPrimitive != null) {
      jsonPrimitive.longOrNull?.let { return it.toInt() }
      jsonPrimitive.doubleOrNull?.let { return it.toInt() }
      jsonPrimitive.contentOrNull?.toDoubleOrNull()?.let { return it.toInt() }
    }
    return decoder.decodeInt()
  }
}

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
