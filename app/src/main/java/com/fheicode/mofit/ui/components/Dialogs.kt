package com.fheicode.mofit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fheicode.mofit.data.EXERCISE_EMOJIS
import com.fheicode.mofit.data.Exercise
import com.fheicode.mofit.data.MUSCLE_GROUPS
import com.fheicode.mofit.data.PlanItem
import com.fheicode.mofit.data.SetLog
import com.fheicode.mofit.ui.setLabel
import com.fheicode.mofit.ui.theme.MoFitColors

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MoFitColors.Lime,
    selectedLabelColor = Color.Black,
    containerColor = MoFitColors.SurfaceHigh,
    labelColor = MoFitColors.TextPrimary,
)

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MoFitColors.Lime, contentColor = Color.Black),
        modifier = modifier.height(56.dp),
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}

/** Bottom sheet for logging one set of an exercise. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetSheet(
    exercise: Exercise,
    initialWeight: Double,
    initialReps: Int,
    best: SetLog?,
    onDismiss: () -> Unit,
    onSave: (weightKg: Double, reps: Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var weight by remember { mutableStateOf(initialWeight) }
    var reps by remember { mutableStateOf(initialReps.toDouble()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MoFitColors.Surface,
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseAvatar(exercise, size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("SATZ ERFASSEN", style = MaterialTheme.typography.labelSmall, color = MoFitColors.Lime)
                    Text(exercise.name, style = MaterialTheme.typography.headlineSmall)
                }
            }
            if (best != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MoFitColors.SurfaceHigh)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.EmojiEvents, null, tint = MoFitColors.Yellow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Dein Rekord: ${setLabel(best.weightKg, best.reps)}", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(20.dp))
            NumberStepper(
                label = if (exercise.bodyweight) "Zusatzgewicht" else "Gewicht",
                value = weight,
                onValueChange = { weight = it },
                step = if (exercise.bodyweight) 5.0 else 2.5,
                unit = "kg",
                decimals = true,
            )
            Spacer(Modifier.height(16.dp))
            NumberStepper(
                label = "Wiederholungen",
                value = reps,
                onValueChange = { reps = it },
                step = 1.0,
                unit = "Wdh",
                min = 1.0,
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                "Satz speichern 🔥",
                onClick = { onSave(weight, reps.toInt()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = reps >= 1,
            )
        }
    }
}

/** Create or edit a weekly plan entry. */
@Composable
fun PlanItemDialog(
    initial: PlanItem,
    exercises: List<Exercise>,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (PlanItem) -> Unit,
) {
    var exerciseId by remember { mutableStateOf(initial.exerciseId) }
    var sets by remember { mutableStateOf(initial.sets.toDouble()) }
    var reps by remember { mutableStateOf(initial.reps.toDouble()) }
    var weight by remember { mutableStateOf(initial.weightKg) }
    val selected = exercises.find { it.id == exerciseId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MoFitColors.Surface,
        title = { Text(if (isNew) "Übung einplanen" else "Plan bearbeiten") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("ÜBUNG", style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exercises, key = { it.id }) { ex ->
                        FilterChip(
                            selected = ex.id == exerciseId,
                            onClick = { exerciseId = ex.id },
                            label = { Text("${ex.emoji} ${ex.name}") },
                            colors = chipColors(),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                NumberStepper("Sätze", sets, { sets = it }, step = 1.0, unit = "", min = 1.0)
                Spacer(Modifier.height(12.dp))
                NumberStepper("Wiederholungen", reps, { reps = it }, step = 1.0, unit = "Wdh", min = 1.0)
                Spacer(Modifier.height(12.dp))
                NumberStepper(
                    if (selected?.bodyweight == true) "Zusatzgewicht" else "Gewicht",
                    weight, { weight = it },
                    step = 2.5, unit = "kg", decimals = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = {
                    onSave(initial.copy(exerciseId = exerciseId, sets = sets.toInt(), reps = reps.toInt(), weightKg = weight))
                },
            ) { Text("Speichern", color = MoFitColors.Lime) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = MoFitColors.TextSecondary) } },
    )
}

/** Create or edit an exercise type. */
@Composable
fun ExerciseDialog(
    initial: Exercise?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var group by remember { mutableStateOf(initial?.muscleGroup ?: MUSCLE_GROUPS.first()) }
    var emoji by remember { mutableStateOf(initial?.emoji ?: EXERCISE_EMOJIS.first()) }
    var colorIndex by remember { mutableIntStateOf(initial?.colorIndex ?: 0) }
    var bodyweight by remember { mutableStateOf(initial?.bodyweight ?: false) }
    val preview = Exercise(id = "preview", name = name, muscleGroup = group, emoji = emoji, colorIndex = colorIndex, bodyweight = bodyweight)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MoFitColors.Surface,
        title = { Text(if (initial == null) "Neue Übung" else "Übung bearbeiten") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseAvatar(preview, size = 56.dp)
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoFitColors.Lime,
                            focusedLabelColor = MoFitColors.Lime,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("MUSKELGRUPPE", style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MUSCLE_GROUPS) { g ->
                        FilterChip(selected = g == group, onClick = { group = g }, label = { Text(g) }, colors = chipColors())
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("ICON", style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(EXERCISE_EMOJIS) { e ->
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (e == emoji) MoFitColors.Lime.copy(alpha = 0.25f) else MoFitColors.SurfaceHigh)
                                .border(1.5.dp, if (e == emoji) MoFitColors.Lime else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center,
                        ) { Text(e, fontSize = 22.sp) }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("FARBE", style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoFitColors.Palette.forEachIndexed { i, c ->
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(3.dp, if (i == colorIndex) Color.White else Color.Transparent, CircleShape)
                                .clickable { colorIndex = i },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (i == colorIndex) Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Körpergewicht", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "z. B. Klimmzüge – Fortschritt zählt über Wiederholungen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoFitColors.TextSecondary,
                        )
                    }
                    Switch(
                        checked = bodyweight,
                        onCheckedChange = { bodyweight = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = MoFitColors.Lime, checkedThumbColor = Color.Black),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val base = initial ?: preview.copy(id = com.fheicode.mofit.data.newId())
                    onSave(base.copy(name = name.trim(), muscleGroup = group, emoji = emoji, colorIndex = colorIndex, bodyweight = bodyweight))
                },
            ) { Text("Speichern", color = MoFitColors.Lime) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = MoFitColors.TextSecondary) } },
    )
}

/** Simple list to choose any exercise (for training outside the plan). */
@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MoFitColors.Surface,
        title = { Text("Welche Übung?") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(exercises, key = { it.id }) { ex ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPick(ex) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ExerciseAvatar(ex, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(ex.name, style = MaterialTheme.typography.titleSmall)
                            Text(ex.muscleGroup, style = MaterialTheme.typography.bodySmall, color = MoFitColors.TextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = MoFitColors.TextSecondary) } },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MoFitColors.Surface,
        title = { Text(title) },
        text = { Text(text, color = MoFitColors.TextSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, color = MoFitColors.Red) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen", color = MoFitColors.TextSecondary) } },
    )
}
