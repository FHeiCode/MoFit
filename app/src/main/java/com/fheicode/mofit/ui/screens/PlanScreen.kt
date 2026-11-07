package com.fheicode.mofit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.data.AppData
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.data.PlanItem
import com.fheicode.mofit.ui.DAY_FULL
import com.fheicode.mofit.ui.DAY_SHORT
import com.fheicode.mofit.ui.components.EmptyHint
import com.fheicode.mofit.ui.components.ExerciseAvatar
import com.fheicode.mofit.ui.components.MoCard
import com.fheicode.mofit.ui.components.PlanItemDialog
import com.fheicode.mofit.ui.components.ScreenHeader
import com.fheicode.mofit.ui.planLabel
import com.fheicode.mofit.ui.theme.MoFitColors
import java.time.LocalDate

@Composable
fun PlanScreen(
    data: AppData,
    repository: MoFitRepository,
    onMessage: (String) -> Unit,
) {
    val todayDow = LocalDate.now().dayOfWeek.value
    var selectedDay by rememberSaveable { mutableIntStateOf(todayDow) }
    var editing by remember { mutableStateOf<PlanItem?>(null) }
    var editingIsNew by remember { mutableStateOf(false) }

    val exercisesById = data.exercises.associateBy { it.id }
    val dayItems = data.plan
        .filter { it.dayOfWeek == selectedDay && it.exerciseId in exercisesById }
        .sortedBy { it.order }
    val countByDay = data.plan.filter { it.exerciseId in exercisesById }.groupingBy { it.dayOfWeek }.eachCount()

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(title = "Wochenplan", subtitle = "${countByDay.size} Trainingstage") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (day in 1..7) {
                    val selected = day == selectedDay
                    val count = countByDay[day] ?: 0
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) MoFitColors.Lime else MoFitColors.Surface)
                            .border(
                                1.5.dp,
                                when {
                                    selected -> MoFitColors.Lime
                                    day == todayDow -> MoFitColors.Lime.copy(alpha = 0.6f)
                                    else -> MoFitColors.Outline
                                },
                                RoundedCornerShape(16.dp),
                            )
                            .clickable { selectedDay = day }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            DAY_SHORT[day - 1],
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) Color.Black else MoFitColors.TextPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        count == 0 -> Color.Transparent
                                        selected -> Color.Black
                                        else -> MoFitColors.Pink
                                    }
                                )
                        )
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text(DAY_FULL[selectedDay - 1], style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (dayItems.isNotEmpty()) {
                    Text(
                        "${dayItems.sumOf { it.sets }} Sätze",
                        style = MaterialTheme.typography.labelLarge,
                        color = MoFitColors.TextSecondary,
                    )
                }
            }
        }

        if (dayItems.isEmpty()) {
            item {
                EmptyHint(
                    emoji = "🛋️",
                    title = "Noch nichts geplant",
                    text = if (data.exercises.isEmpty()) "Leg zuerst unter „Übungen“ ein paar Übungen an."
                    else "Ruhetag – oder füg unten eine Übung hinzu.",
                )
            }
        }

        items(dayItems, key = { it.id }) { item ->
            val exercise = exercisesById.getValue(item.exerciseId)
            MoCard(
                Modifier.fillMaxWidth(),
                onClick = { editing = item; editingIsNew = false },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseAvatar(exercise)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            planLabel(item.sets, item.weightKg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MoFitColors.palette(exercise.colorIndex),
                        )
                    }
                    IconButton(onClick = { editing = item; editingIsNew = false }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Bearbeiten", tint = MoFitColors.TextSecondary)
                    }
                    IconButton(onClick = {
                        repository.deletePlanItem(item.id)
                        onMessage("${exercise.name} aus ${DAY_FULL[selectedDay - 1]} entfernt")
                    }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Entfernen", tint = MoFitColors.TextSecondary)
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val first = data.exercises.first()
                    editing = PlanItem(
                        dayOfWeek = selectedDay,
                        exerciseId = first.id,
                        sets = 3,
                        weightKg = 0.0,
                        order = (dayItems.maxOfOrNull { it.order } ?: -1) + 1,
                    )
                    editingIsNew = true
                },
                enabled = data.exercises.isNotEmpty(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoFitColors.Lime, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Übung hinzufügen")
            }
        }
    }

    editing?.let { item ->
        PlanItemDialog(
            initial = item,
            exercises = data.exercises,
            isNew = editingIsNew,
            onDismiss = { editing = null },
            onSave = {
                repository.savePlanItem(it)
                editing = null
            },
        )
    }
}
