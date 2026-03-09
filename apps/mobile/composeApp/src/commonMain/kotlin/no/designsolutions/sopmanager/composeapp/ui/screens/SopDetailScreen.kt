package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import no.designsolutions.sopmanager.composeapp.logging.AppLogger
import no.designsolutions.sopmanager.composeapp.model.ProcedureDetail

@Composable
fun SopDetailScreen(
    partNumber: String,
    detail: ProcedureDetail?,
    onCreate: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Step ${index + 1}")
                        if (photo.previewUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No image URL",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        } else {
                            KamelImage(
                                resource = { asyncPainterResource(photo.previewUrl) },
                                contentDescription = "Photo step ${index + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable {
                                        try {
                                            uriHandler.openUri(photo.previewUrl)
                                        } catch (error: Throwable) {
                                            AppLogger.e(
                                                "Failed to open image URI for part=$partNumber step=${index + 1} url=${photo.previewUrl}",
                                                error,
                                            )
                                        }
                                    },
                                onLoading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "Loading image...",
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                },
                                onFailure = {
                                    AppLogger.e(
                                        "SOP detail image load failed for part=$partNumber step=${index + 1} url=${photo.previewUrl}",
                                        it,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "Failed to load image",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                },
                            )
                        }
                        Text(
                            text = description,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
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
}
