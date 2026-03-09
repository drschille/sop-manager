package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import no.designsolutions.sopmanager.composeapp.model.PartSearchResult

@Composable
fun SearchScreen(
    partNumber: String,
    onPartNumberChange: (String) -> Unit,
    results: List<PartSearchResult>,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onSelectResult: (PartSearchResult) -> Unit,
) {
    Text("Search", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
        value = partNumber,
        onValueChange = onPartNumberChange,
        label = { Text("Part number") },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onSearch) {
            Text("Search")
        }
        Button(onClick = onBack) {
            Text("Back")
        }
    }

    if (results.isNotEmpty()) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectResult(result) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (result.thumbnailUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "NO IMG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        } else {
                            KamelImage(
                                resource = { asyncPainterResource(result.thumbnailUrl) },
                                contentDescription = "Thumbnail for ${result.partNumber}",
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .aspectRatio(1f),
                                onLoading = {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "IMG",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                },
                                onFailure = {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "ERR",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                },
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = result.partNumber,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = result.sopTitle ?: "No procedure title yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
