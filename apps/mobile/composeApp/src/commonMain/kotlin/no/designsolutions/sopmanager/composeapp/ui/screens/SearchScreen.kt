package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectResult(result) },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(result.partNumber)
                        Text(result.sopTitle ?: "No SOP title yet")
                    }
                }
            }
        }
    }
}
