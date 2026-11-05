package com.fheicode.mofit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.BuildConfigInfo
import com.fheicode.mofit.data.AppData
import com.fheicode.mofit.data.Exercise
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.ui.components.ConfirmDialog
import com.fheicode.mofit.ui.components.EmptyHint
import com.fheicode.mofit.ui.components.ExerciseAvatar
import com.fheicode.mofit.ui.components.ExerciseDialog
import com.fheicode.mofit.ui.components.MoCard
import com.fheicode.mofit.ui.components.ScreenHeader
import com.fheicode.mofit.ui.theme.MoFitColors

@Composable
fun ExercisesScreen(
    data: AppData,
    repository: MoFitRepository,
    onMessage: (String) -> Unit,
) {
    var editing by remember { mutableStateOf<Exercise?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Exercise?>(null) }
    val versionName = BuildConfigInfo.versionName(LocalContext.current)

    val groups = data.exercises.sortedBy { it.name.lowercase() }.groupBy { it.muscleGroup }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeader(title = "Übungen", subtitle = "${data.exercises.size} Übungsarten") }

        item {
            Button(
                onClick = { creating = true },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoFitColors.Lime, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Neue Übung")
            }
        }

        if (data.exercises.isEmpty()) {
            item { EmptyHint("🏋️", "Noch keine Übungen", "Leg deine erste Übung an – z. B. Bankdrücken oder Klimmzüge.") }
        }

        groups.forEach { (group, exercises) ->
            item(key = "group-$group") {
                Text(
                    group.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MoFitColors.TextSecondary,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            items(exercises, key = { it.id }) { ex ->
                val plannedDays = data.plan.count { it.exerciseId == ex.id }
                val sets = data.logs.count { it.exerciseId == ex.id }
                MoCard(Modifier.fillMaxWidth(), onClick = { editing = ex }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExerciseAvatar(ex)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ex.name, style = MaterialTheme.typography.titleMedium)
                                if (ex.bodyweight) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "KÖRPERGEWICHT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MoFitColors.Cyan,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MoFitColors.Cyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                "$plannedDays× im Plan · $sets Sätze erfasst",
                                style = MaterialTheme.typography.bodySmall,
                                color = MoFitColors.TextSecondary,
                            )
                        }
                        IconButton(onClick = { deleting = ex }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Löschen", tint = MoFitColors.TextSecondary)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "MoFit $versionName",
                style = MaterialTheme.typography.labelSmall,
                color = MoFitColors.TextSecondary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }

    if (creating || editing != null) {
        ExerciseDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = {
                repository.saveExercise(it)
                onMessage(if (editing == null) "${it.emoji} ${it.name} angelegt" else "${it.name} gespeichert")
                creating = false
                editing = null
            },
        )
    }

    deleting?.let { ex ->
        val sets = data.logs.count { it.exerciseId == ex.id }
        val planned = data.plan.count { it.exerciseId == ex.id }
        ConfirmDialog(
            title = "${ex.name} löschen?",
            text = buildString {
                append("Die Übung wird endgültig gelöscht")
                if (planned > 0 || sets > 0) append(" – inklusive $planned Plan-Einträgen und $sets erfassten Sätzen")
                append(".")
            },
            confirmLabel = "Löschen",
            onDismiss = { deleting = null },
            onConfirm = {
                repository.deleteExercise(ex.id)
                onMessage("${ex.name} gelöscht")
                deleting = null
            },
        )
    }
}
