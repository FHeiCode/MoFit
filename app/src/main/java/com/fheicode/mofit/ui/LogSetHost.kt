package com.fheicode.mofit.ui

import androidx.compose.runtime.Composable
import com.fheicode.mofit.data.AppData
import com.fheicode.mofit.data.Exercise
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.data.PlanItem
import com.fheicode.mofit.data.Stats
import com.fheicode.mofit.ui.components.LogSetSheet

/** Which exercise to log, with the values the sheet starts with. */
data class LogRequest(val exerciseId: String, val weightKg: Double, val reps: Int)

/**
 * Sensible starting values: the last set from today, otherwise the plan,
 * otherwise the last set ever, otherwise 10 reps without weight.
 */
fun defaultLogRequest(data: AppData, exercise: Exercise, plan: PlanItem?, todayEpochDay: Long): LogRequest {
    val logs = data.logs.filter { it.exerciseId == exercise.id }
    val lastToday = logs.filter { it.epochDay == todayEpochDay }.maxByOrNull { it.timestamp }
    val lastEver = logs.maxByOrNull { it.timestamp }
    return when {
        lastToday != null -> LogRequest(exercise.id, lastToday.weightKg, lastToday.reps)
        plan != null -> LogRequest(exercise.id, plan.weightKg, plan.reps)
        lastEver != null -> LogRequest(exercise.id, lastEver.weightKg, lastEver.reps)
        else -> LogRequest(exercise.id, 0.0, 10)
    }
}

@Composable
fun LogSetHost(
    request: LogRequest?,
    data: AppData,
    repository: MoFitRepository,
    onMessage: (String) -> Unit,
    onClose: () -> Unit,
) {
    if (request == null) return
    val exercise = data.exercises.find { it.id == request.exerciseId } ?: return
    LogSetSheet(
        exercise = exercise,
        initialWeight = request.weightKg,
        initialReps = request.reps,
        best = Stats.bestSet(data.logs, exercise),
        onDismiss = onClose,
        onSave = { weight, reps ->
            val record = Stats.isNewRecord(data.logs, exercise, weight, reps)
            repository.logSet(exercise.id, weight, reps)
            onClose()
            onMessage(
                if (record) "🏆 Neuer Rekord bei ${exercise.name}: ${setLabel(weight, reps)}!"
                else "💪 ${exercise.name}: ${setLabel(weight, reps)} gespeichert"
            )
        },
    )
}
