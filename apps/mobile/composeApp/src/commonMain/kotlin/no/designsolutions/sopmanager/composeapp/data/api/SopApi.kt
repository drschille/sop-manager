package no.designsolutions.sopmanager.composeapp.data.api

import com.kansson.kmp.convex.core.ConvexFunction
import kotlinx.serialization.Serializable
import no.designsolutions.sopmanager.composeapp.data.repository.PartResponse
import no.designsolutions.sopmanager.composeapp.data.repository.ProcedureResponse
import no.designsolutions.sopmanager.composeapp.data.repository.VersionResponse

object SopApi {
  object Procedures {
    data class GetByPartNumber(
        override val identifier: String = "procedures:getByPartNumber",
        override val args: ByPartArgs,
    ) : ConvexFunction.Query<ByPartArgs, ProcedureByPartResponse> {
        constructor(partNumber: String) : this(args = ByPartArgs(partNumber))
    }
  }
}

@Serializable data class ByPartArgs(val partNumber: String)

@Serializable
data class ProcedureByPartResponse(
    val part: PartResponse? = null,
    val procedure: ProcedureResponse? = null,
    val currentVersion: VersionResponse? = null,
)
