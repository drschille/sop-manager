package no.designsolutions.sopmanager.composeapp.model

data class PhotoRef(
    val storageId: String,
    val previewUrl: String? = null,
)

data class SopVersion(
    val id: String,
    val versionNumber: Int,
    val title: String,
    val body: String,
    val photos: List<PhotoRef>,
    val createdAt: String,
    val createdBy: String,
)

data class ProcedureDetail(
    val procedureId: String,
    val partNumber: String,
    val latest: SopVersion?,
)
