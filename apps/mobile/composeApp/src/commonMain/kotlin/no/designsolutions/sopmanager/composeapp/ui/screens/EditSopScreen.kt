package no.designsolutions.sopmanager.composeapp.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import no.designsolutions.sopmanager.composeapp.StepDraft

@Composable
fun EditSopScreen(
    title: String,
    body: String,
    steps: List<StepDraft>,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onStepDescriptionChange: (Int, String) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (Int) -> Unit,
    onMoveStep: (Int, Int) -> Unit,
    onAttachMedia: (Int) -> Unit,
    onCaptureMedia: (Int) -> Unit,
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

    Text("Steps")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(steps) { index, step ->
            val dragDistance = remember { mutableFloatStateOf(0f) }
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .pointerInput(index, steps.size) {
                                detectDragGestures(
                                    onDragEnd = { dragDistance.floatValue = 0f },
                                    onDragCancel = { dragDistance.floatValue = 0f },
                                ) { change, dragAmount ->
                                    change.consume()
                                    dragDistance.floatValue += dragAmount.y
                                    if (dragDistance.floatValue > 36f && index < steps.lastIndex) {
                                        onMoveStep(index, index + 1)
                                        dragDistance.floatValue = 0f
                                    } else if (dragDistance.floatValue < -36f && index > 0) {
                                        onMoveStep(index, index - 1)
                                        dragDistance.floatValue = 0f
                                    }
                                }
                            },
                    ) {
                        Text("⋮⋮")
                    }

                    OutlinedTextField(
                        value = step.description,
                        onValueChange = { onStepDescriptionChange(index, it) },
                        label = { Text("Step ${index + 1}") },
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 48.dp, top = 6.dp),
                ) {
                    OutlinedButton(onClick = { onAttachMedia(index) }) {
                        Text("Attach photo/video")
                    }
                    OutlinedButton(onClick = { onCaptureMedia(index) }) {
                        Text("Capture")
                    }
                    OutlinedButton(onClick = { onRemoveStep(index) }) {
                        Text("Remove")
                    }
                }

                if (step.media.isNotEmpty()) {
                    Text(
                        text = "${step.media.size} media item(s) attached",
                        modifier = Modifier.padding(start = 48.dp, top = 4.dp),
                    )
                }
            }
        }
    }

    Button(onClick = onAddStep) {
        Text("Add Step")
    }

    Button(onClick = onSave) {
        Text("Save SOP")
    }
}
