package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.designsolutions.sopmanager.composeapp.model.SopVersion

@Composable
fun HistoryScreen(
    versions: List<SopVersion>,
    selectedVersion: SopVersion?,
    onSelectVersion: (SopVersion) -> Unit,
    onBack: () -> Unit,
) {
    Text("Version History", style = MaterialTheme.typography.headlineMedium)

    if (versions.isEmpty()) {
        Text("No versions found")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(versions) { version ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectVersion(version) },
                ) {
                    Text("v${version.versionNumber} - ${version.title} (${version.createdBy})")
                }
            }
        }
    }

    selectedVersion?.let { version ->
        Text("Selected: v${version.versionNumber}")
        Text(version.body)
    }

    Button(onClick = onBack) {
        Text("Back")
    }
}
