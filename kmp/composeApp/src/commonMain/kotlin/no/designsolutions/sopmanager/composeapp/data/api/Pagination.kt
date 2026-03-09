package no.designsolutions.timetracker.composeapp.data.api

import com.kansson.kmp.convex.core.ConvexDsl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = PaginationOpts.Serializer::class)
data class PaginationOpts(
    val cursor: String? = null,
    val endCursor: String? = null,
    val id: Double? = null,
    val maximumBytesRead: Double? = null,
    val maximumRowsRead: Double? = null,
    val numItems: Double,
) {
    @ConvexDsl
    class Builder {
        var cursor: String? = null

        var endCursor: String? = null

        var id: Double? = null

        var maximumBytesRead: Double? = null

        var maximumRowsRead: Double? = null

        var numItems: Double = 20.0

        fun build(): PaginationOpts =
            PaginationOpts(
                cursor = cursor,
                endCursor = endCursor,
                id = id,
                maximumBytesRead = maximumBytesRead,
                maximumRowsRead = maximumRowsRead,
                numItems = numItems,
            )
    }

    class Serializer : KSerializer<PaginationOpts> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("PaginationOpts") {
                element<String?>("cursor", isOptional = false)
                element<String?>("endCursor", isOptional = true)
            }

        override fun serialize(encoder: Encoder, value: PaginationOpts) {
            require(encoder is JsonEncoder)
            val obj = buildJsonObject {
                put("cursor", value.cursor?.let { JsonPrimitive(it) } ?: JsonNull)
                put("numItems", JsonPrimitive(value.numItems))
                value.endCursor?.let { put("endCursor", JsonPrimitive(it)) }
                value.id?.let { put("id", JsonPrimitive(it)) }
                value.maximumBytesRead?.let { put("maximumBytesRead", JsonPrimitive(it)) }
                value.maximumRowsRead?.let { put("maximumRowsRead", JsonPrimitive(it)) }
            }
            encoder.encodeJsonElement(obj)
        }

        override fun deserialize(decoder: Decoder): PaginationOpts {
            require(decoder is JsonDecoder)
            val obj = decoder.decodeJsonElement().jsonObject
            return PaginationOpts(
                cursor = obj["cursor"]?.jsonPrimitive?.contentOrNull,
                endCursor = obj["endCursor"]?.jsonPrimitive?.contentOrNull,
                id = obj["id"]?.jsonPrimitive?.doubleOrNull,
                maximumBytesRead = obj["maximumBytesRead"]?.jsonPrimitive?.doubleOrNull,
                maximumRowsRead = obj["maximumRowsRead"]?.jsonPrimitive?.doubleOrNull,
                numItems =
                    obj["numItems"]?.jsonPrimitive?.doubleOrNull
                        ?: throw IllegalArgumentException("numItems is required"),
            )
        }
    }

    companion object {
        operator fun invoke(block: Builder.() -> Unit): PaginationOpts =
            Builder().apply(block).build()
    }
}

