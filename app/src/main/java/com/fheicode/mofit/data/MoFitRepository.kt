package com.fheicode.mofit.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

/**
 * Holds all app data in memory and mirrors every change to a JSON file in app storage.
 * The file survives app updates, so data is kept when a new release is installed.
 */
class MoFitRepository(
    private val file: File,
    private val scope: CoroutineScope,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val writeMutex = Mutex()

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        if (!file.exists()) persist()
    }

    private fun load(): AppData {
        if (!file.exists()) return SampleData.create()
        return try {
            val loaded = json.decodeFromString(AppData.serializer(), file.readText())
            loaded.copy(exercises = loaded.exercises.map(::migrateMuscleGroup))
        } catch (e: Exception) {
            // Keep the unreadable file for inspection instead of silently losing it.
            file.copyTo(File(file.parentFile, "${file.name}.corrupt-${System.currentTimeMillis()}"), overwrite = true)
            SampleData.create()
        }
    }

    private fun persist() {
        scope.launch(Dispatchers.IO) {
            writeMutex.withLock {
                val text = json.encodeToString(AppData.serializer(), _data.value)
                val tmp = File(file.parentFile, "${file.name}.tmp")
                tmp.writeText(text)
                if (!tmp.renameTo(file)) {
                    tmp.copyTo(file, overwrite = true)
                    tmp.delete()
                }
            }
        }
    }

    private fun mutate(transform: (AppData) -> AppData) {
        _data.update(transform)
        persist()
    }

    // --- Exercises ---

    fun saveExercise(exercise: Exercise) = mutate { d ->
        d.copy(exercises = d.exercises.upsert(exercise) { it.id })
    }

    /** Deletes the exercise together with its plan entries and logged sets. */
    fun deleteExercise(id: String) = mutate { d ->
        d.copy(
            exercises = d.exercises.filterNot { it.id == id },
            plan = d.plan.filterNot { it.exerciseId == id },
            logs = d.logs.filterNot { it.exerciseId == id },
        )
    }

    // --- Weekly plan ---

    fun savePlanItem(item: PlanItem) = mutate { d ->
        d.copy(plan = d.plan.upsert(item) { it.id })
    }

    fun deletePlanItem(id: String) = mutate { d ->
        d.copy(plan = d.plan.filterNot { it.id == id })
    }

    // --- Tracking ---

    fun logSet(exerciseId: String, weightKg: Double, reps: Int, date: LocalDate = LocalDate.now()) {
        val log = SetLog(
            exerciseId = exerciseId,
            epochDay = date.toEpochDay(),
            timestamp = System.currentTimeMillis(),
            weightKg = weightKg,
            reps = reps,
        )
        mutate { d -> d.copy(logs = d.logs + log) }
    }

    /** Logs several sets at once (used by "Plan übernehmen"). */
    fun logSets(entries: List<SetEntry>, date: LocalDate = LocalDate.now()) {
        if (entries.isEmpty()) return
        val now = System.currentTimeMillis()
        val logs = entries.mapIndexed { i, e ->
            SetLog(
                exerciseId = e.exerciseId,
                epochDay = date.toEpochDay(),
                timestamp = now + i, // keeps the order stable
                weightKg = e.weightKg,
                reps = e.reps,
            )
        }
        mutate { d -> d.copy(logs = d.logs + logs) }
    }

    fun deleteLog(id: String) = mutate { d ->
        d.copy(logs = d.logs.filterNot { it.id == id })
    }
}

private fun <T> List<T>.upsert(item: T, key: (T) -> Any): List<T> {
    val k = key(item)
    return if (any { key(it) == k }) map { if (key(it) == k) item else it } else this + item
}
