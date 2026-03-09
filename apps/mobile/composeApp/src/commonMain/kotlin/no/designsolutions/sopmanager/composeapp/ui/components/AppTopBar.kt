package no.designsolutions.sopmanager.composeapp.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import sop_manager_compose.composeapp.generated.resources.Res
import sop_manager_compose.composeapp.generated.resources.back_arrow

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
                    Icon(
                        painter = painterResource(Res.drawable.back_arrow),
                        contentDescription = null,
                    )
                    Text(backLabel ?: "Back")
                }
            }
        },
    )
}
