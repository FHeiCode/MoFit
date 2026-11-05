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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.data.AppData
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.data.Stats
import com.fheicode.mofit.ui.LogRequest
import com.fheicode.mofit.ui.LogSetHost
import com.fheicode.mofit.ui.components.ExerciseAvatar
import com.fheicode.mofit.ui.components.ExercisePickerDialog
import com.fheicode.mofit.ui.components.GradientCard
import com.fheicode.mofit.ui.components.MoCard
import com.fheicode.mofit.ui.components.ScreenHeader
import com.fheicode.mofit.ui.components.SetDots
import com.fheicode.mofit.ui.components.StatTile
import com.fheicode.mofit.ui.defaultLogRequest
import com.fheicode.mofit.ui.formatKg
import com.fheicode.mofit.ui.formatLongDate
import com.fheicode.mofit.ui.planLabel
import com.fheicode.mofit.ui.setLabel
import com.fheicode.mofit.ui.theme.MoFitColors
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    data: AppData,
    repository: MoFitRepository,
    onMessage: (String) -> Unit,
) {
    val today = LocalDate.now()
    val todayEpoch = today.toEpochDay()
    val exercisesById = data.exercises.associateBy { it.id }
    val planToday = data.plan
        .filter { it.dayOfWeek == today.dayOfWeek.value && it.exerciseId in exercisesById }
        .sortedBy { it.order }
    val logsToday = data.logs.filter { it.epochDay == todayEpoch }.sortedByDescending { it.timestamp }
    val doneByExercise = logsToday.groupingBy { it.exerciseId }.eachCount()

    val plannedSets = planToday.sumOf { it.sets }
    val doneSets = planToday.sumOf { minOf(doneByExercise[it.exerciseId] ?: 0, it.sets) }
    val progress = if (plannedSets == 0) 0f else doneSets.toFloat() / plannedSets
    val volumeToday = logsToday.sumOf { it.weightKg * it.reps }
    val streak = Stats.weekStreak(data.logs, todayEpoch)

    var logRequest by remember { mutableStateOf<LogRequest?>(null) }
    var picking by remember { mutableStateOf(false) }

    val greeting = when (LocalTime.now().hour) {
        in 5..10 -> "Guten Morgen ☀️"
        in 11..17 -> "Hey, Champion 👋"
        else -> "Guten Abend 🌙"
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(title = greeting, subtitle = formatLongDate(today)) }

        item {
            GradientCard(Modifier.fillMaxWidth()) {
                if (planToday.isEmpty()) {
                    Text("Ruhetag 😴", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Muskeln wachsen in der Pause. Lust auf eine Extra-Runde? Dann leg einfach los.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                } else {
                    Text(
                        when {
                            progress >= 1f -> "Alles erledigt! Beast Mode 🦾"
                            progress >= 0.5f -> "Fast geschafft, bleib dran! ⚡"
                            progress > 0f -> "Guter Start! 🔥"
                            else -> "Heute wird's stark 🚀"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$doneSets", style = MaterialTheme.typography.displayMedium, color = Color.White)
                        Text(
                            " / $plannedSets Sätze",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("🔥", "$streak", if (streak == 1) "Woche Streak" else "Wochen Streak", Modifier.weight(1f), MoFitColors.Orange)
                StatTile("✅", "${logsToday.size}", "Sätze heute", Modifier.weight(1f), MoFitColors.Lime)
                StatTile("🏋️", formatKg(volumeToday.toInt().toDouble()), "kg bewegt", Modifier.weight(1f), MoFitColors.Cyan)
            }
        }

        if (planToday.isNotEmpty()) {
            item {
                Text(
                    "DEIN PLAN HEUTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MoFitColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        items(planToday, key = { it.id }) { item ->
            val exercise = exercisesById.getValue(item.exerciseId)
            val done = doneByExercise[item.exerciseId] ?: 0
            val complete = done >= item.sets
            val color = MoFitColors.palette(exercise.colorIndex)
            MoCard(
                Modifier.fillMaxWidth(),
                borderColor = if (complete) MoFitColors.Lime else MoFitColors.Outline,
                onClick = { logRequest = defaultLogRequest(data, exercise, item, todayEpoch) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseAvatar(exercise)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            planLabel(item.sets, item.reps, item.weightKg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MoFitColors.TextSecondary,
                        )
                    }
                    FilledIconButton(
                        onClick = { logRequest = defaultLogRequest(data, exercise, item, todayEpoch) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (complete) MoFitColors.Lime else MoFitColors.SurfaceHigh,
                            contentColor = if (complete) Color.Black else MoFitColors.Lime,
                        ),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(if (complete) Icons.Rounded.Check else Icons.Rounded.Add, contentDescription = "Satz erfassen")
                    }
                }
                Spacer(Modifier.height(12.dp))
                SetDots(done = done, total = item.sets, color = color)
            }
        }

        item {
            Button(
                onClick = { picking = true },
                enabled = data.exercises.isNotEmpty(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoFitColors.SurfaceHigh,
                    contentColor = MoFitColors.Lime,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(54.dp),
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (planToday.isEmpty()) "Freies Training starten" else "Andere Übung erfassen")
            }
        }

        if (logsToday.isNotEmpty()) {
            item {
                Text(
                    "HEUTE ERFASST",
                    style = MaterialTheme.typography.labelSmall,
                    color = MoFitColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(logsToday, key = { "log-" + it.id }) { log ->
                val exercise = exercisesById[log.exerciseId]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MoFitColors.Surface)
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExerciseAvatar(exercise, size = 34.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exercise?.name ?: "?", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${setLabel(log.weightKg, log.reps)} · ${timeFormat.format(Date(log.timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoFitColors.TextSecondary,
                        )
                    }
                    IconButton(onClick = {
                        repository.deleteLog(log.id)
                        onMessage("Satz gelöscht")
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Satz löschen", tint = MoFitColors.TextSecondary)
                    }
                }
            }
        }
    }

    if (picking) {
        ExercisePickerDialog(
            exercises = data.exercises,
            onDismiss = { picking = false },
            onPick = { ex ->
                picking = false
                val plan = planToday.find { it.exerciseId == ex.id }
                logRequest = defaultLogRequest(data, ex, plan, todayEpoch)
            },
        )
    }

    LogSetHost(logRequest, data, repository, onMessage, onClose = { logRequest = null })
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)
