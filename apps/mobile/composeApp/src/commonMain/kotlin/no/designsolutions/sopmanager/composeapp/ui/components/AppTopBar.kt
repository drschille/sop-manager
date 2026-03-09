package no.designsolutions.sopmanager.composeapp.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppTopBar(
    title: String,
    canNavigateBack: Boolean,
    backLabel: String?,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                TextButton(onClick = onBack) {
                    Text("← ${backLabel ?: "Back"}")
                }
            }
        },
    )
}
