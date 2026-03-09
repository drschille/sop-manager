package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail

@Composable
fun SopDetailScreen(
    partNumber: String,
    detail: ProcedureDetail?,
    onCreate: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
) {
    Text("Part: $partNumber")

    val latest = detail?.latest
    if (latest == null) {
        Text("No SOP found yet")
        Button(onClick = onCreate) {
            Text("Create SOP")
        }
    } else {
        Text("Title: ${latest.title}")
        Text("Body: ${latest.body}")
        Text("Photos: ${latest.photos.size}")
        if (latest.photos.isNotEmpty()) {
            latest.photos.forEachIndexed { index, photo ->
                val description = photo.description?.takeIf { it.isNotBlank() } ?: "No instruction"
                Text("Step ${index + 1}: $description")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onEdit) {
                Text("Edit")
            }
            Button(onClick = onHistory) {
                Text("History")
            }
        }
    }
}
