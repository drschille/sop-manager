package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditSopScreen(
    title: String,
    body: String,
    photoDescriptions: List<String>,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onPhotoDescriptionChange: (Int, String) -> Unit,
    onSave: () -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = body,
        onValueChange = onBodyChange,
        label = { Text("Instructions") },
        modifier = Modifier.fillMaxWidth(),
    )

    if (photoDescriptions.isNotEmpty()) {
        Text("Photo steps")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(photoDescriptions.indices.toList()) { index ->
                OutlinedTextField(
                    value = photoDescriptions[index],
                    onValueChange = { onPhotoDescriptionChange(index, it) },
                    label = { Text("Step ${index + 1} description") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    Button(onClick = onSave) {
        Text("Save SOP")
    }
}
