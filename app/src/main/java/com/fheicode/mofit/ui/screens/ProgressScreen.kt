package com.fheicode.mofit.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.data.AppData
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.data.Stats
import com.fheicode.mofit.ui.LogRequest
import com.fheicode.mofit.ui.LogSetHost
import com.fheicode.mofit.ui.components.EmptyHint
import com.fheicode.mofit.ui.components.ExerciseAvatar
import com.fheicode.mofit.ui.components.GradientCard
import com.fheicode.mofit.ui.components.LineChart
import com.fheicode.mofit.ui.components.MoCard
import com.fheicode.mofit.ui.components.ScreenHeader
import com.fheicode.mofit.ui.components.StatTile
import com.fheicode.mofit.ui.defaultLogRequest
import com.fheicode.mofit.ui.formatHistoryDate
import com.fheicode.mofit.ui.formatKg
import com.fheicode.mofit.ui.formatShortDate
import com.fheicode.mofit.ui.setLabel
import com.fheicode.mofit.ui.theme.MoFitColors
import java.time.LocalDate

private enum class Metric(val label: String, val unit: String) {
    WEIGHT("Gewicht", "kg"),
    REPS("Wdh", "Wdh"),
    VOLUME("Volumen", "kg"),
}

@Composable
fun ProgressScreen(
    data: AppData,
    repository: MoFitRepository,
    onMessage: (String) -> Unit,
) {
    val todayEpoch = LocalDate.now().toEpochDay()
    val lastLogByExercise = data.logs.groupBy { it.exerciseId }.mapValues { (_, l) -> l.maxOf { it.timestamp } }
    // Exercises with recent activity first.
    val exercises = data.exercises.sortedByDescending { lastLogByExercise[it.id] ?: 0L }

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val exercise = exercises.find { it.id == selectedId } ?: exercises.firstOrNull()
    var logRequest by remember { mutableStateOf<LogRequest?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(title = "Fortschritt", subtitle = "${data.logs.size} Sätze insgesamt") }

        if (exercise == null) {
            item { EmptyHint("📈", "Noch keine Übungen", "Leg unter „Übungen“ welche an, dann siehst du hier deinen Fortschritt.") }
            return@LazyColumn
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises, key = { it.id }) { ex ->
                    FilterChip(
                        selected = ex.id == exercise.id,
                        onClick = { selectedId = ex.id },
                        label = { Text("${ex.emoji} ${ex.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MoFitColors.Lime,
                            selectedLabelColor = Color.Black,
                            containerColor = MoFitColors.Surface,
                            labelColor = MoFitColors.TextPrimary,
                        ),
                    )
                }
            }
        }

        val logs = data.logs.filter { it.exerciseId == exercise.id }
        val daily = Stats.dailyStats(data.logs, exercise.id)
        val best = Stats.bestSet(data.logs, exercise)
        val color = MoFitColors.palette(exercise.colorIndex)

        item {
            GradientCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseAvatar(exercise, size = 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(exercise.muscleGroup.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                        Text(exercise.name, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("🏆 REKORD", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    best?.let { setLabel(it.weightKg, it.reps) } ?: "Noch kein Satz",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                if (daily.size >= 2) {
                    val first = daily.first()
                    val last = daily.last()
                    val delta = if (exercise.bodyweight) "${signed((last.maxReps - first.maxReps).toDouble())} Wdh"
                    else "${signed(last.maxWeight - first.maxWeight)} kg"
                    Text(
                        "$delta seit ${formatShortDate(first.epochDay)} 📈",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("📅", "${daily.size}", "Trainings", Modifier.weight(1f), MoFitColors.Cyan)
                StatTile("🔁", "${logs.size}", "Sätze", Modifier.weight(1f), MoFitColors.Lime)
                if (exercise.bodyweight) {
                    StatTile("💯", "${logs.sumOf { it.reps }}", "Wdh gesamt", Modifier.weight(1f), MoFitColors.Pink)
                } else {
                    val oneRm = best?.let { Stats.estimatedOneRepMax(it.weightKg, it.reps) } ?: 0.0
                    StatTile("⚡", formatKg(Math.round(oneRm * 2) / 2.0), "kg 1RM*", Modifier.weight(1f), MoFitColors.Pink)
                }
            }
        }

        if (daily.isNotEmpty()) {
            item {
                ChartCard(exercise.id, exercise.bodyweight, daily.takeLast(30), color)
            }
        }

        item {
            Button(
                onClick = { logRequest = defaultLogRequest(data, exercise, null, todayEpoch) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoFitColors.Lime, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Satz erfassen")
            }
        }

        if (logs.isEmpty()) {
            item { EmptyHint("🚀", "Noch keine Daten", "Erfasse deinen ersten Satz – dann wächst hier dein Diagramm.") }
        } else {
            item {
                Text("VERLAUF", style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            val byDay = logs.groupBy { it.epochDay }.entries.sortedByDescending { it.key }
            items(byDay, key = { "day-" + it.key }) { (day, sets) ->
                MoCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatHistoryDate(day), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("${sets.size} Sätze", style = MaterialTheme.typography.labelMedium, color = MoFitColors.TextSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                    sets.sortedBy { it.timestamp }.forEachIndexed { i, set ->
                        val isBest = best?.id == set.id
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Satz ${i + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoFitColors.TextSecondary,
                                modifier = Modifier.width(64.dp),
                            )
                            Text(
                                setLabel(set.weightKg, set.reps) + if (isBest) "  🏆" else "",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isBest) MoFitColors.Yellow else MoFitColors.TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = {
                                    repository.deleteLog(set.id)
                                    onMessage("Satz gelöscht")
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Satz löschen", tint = MoFitColors.TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    LogSetHost(logRequest, data, repository, onMessage, onClose = { logRequest = null })
}

@Composable
private fun ChartCard(
    exerciseId: String,
    bodyweight: Boolean,
    daily: List<com.fheicode.mofit.data.DayStat>,
    color: Color,
) {
    var metric by rememberSaveable(exerciseId) { mutableStateOf(if (bodyweight) Metric.REPS else Metric.WEIGHT) }
    val values = daily.map {
        when (metric) {
            Metric.WEIGHT -> it.maxWeight.toFloat()
            Metric.REPS -> it.maxReps.toFloat()
            Metric.VOLUME -> it.volume.toFloat()
        }
    }
    MoCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Metric.entries.forEach { m ->
                FilterChip(
                    selected = m == metric,
                    onClick = { metric = m },
                    label = { Text(m.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color,
                        selectedLabelColor = Color.Black,
                        containerColor = MoFitColors.SurfaceHigh,
                        labelColor = MoFitColors.TextPrimary,
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            Text(
                "max ${formatKg(values.max().toDouble())} ${metric.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                modifier = Modifier.weight(1f),
            )
            Text(
                "zuletzt ${formatKg(values.last().toDouble())} ${metric.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = MoFitColors.TextSecondary,
            )
        }
        LineChart(
            values = values,
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
        )
        Row {
            Text(formatShortDate(daily.first().epochDay), style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary, modifier = Modifier.weight(1f))
            Text(formatShortDate(daily.last().epochDay), style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
        }
        if (values.size == 1) {
            Spacer(Modifier.height(6.dp))
            Text("Ab dem zweiten Trainingstag siehst du hier eine Kurve.", style = MaterialTheme.typography.bodySmall, color = MoFitColors.TextSecondary)
        }
    }
}

private fun signed(v: Double): String = (if (v >= 0) "+" else "−") + formatKg(kotlin.math.abs(v))
