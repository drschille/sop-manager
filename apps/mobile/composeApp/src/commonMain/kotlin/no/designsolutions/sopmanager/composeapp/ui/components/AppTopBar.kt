package no.designsolutions.sopmanager.composeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    Column(
        modifier = androidx.compose.ui.Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
