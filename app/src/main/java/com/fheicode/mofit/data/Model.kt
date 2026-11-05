package com.fheicode.mofit.data

import kotlinx.serialization.Serializable
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

/** A kind of exercise the user can train, e.g. "Bankdrücken". */
@Serializable
data class Exercise(
    val id: String = newId(),
    val name: String,
    val muscleGroup: String,
    val emoji: String,
    val colorIndex: Int,
    /** Bodyweight exercises are tracked mainly by reps; weight is optional extra load. */
    val bodyweight: Boolean = false,
)

/** One exercise scheduled on a weekday (1 = Monday … 7 = Sunday). */
@Serializable
data class PlanItem(
    val id: String = newId(),
    val dayOfWeek: Int,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
    val order: Int = 0,
)

/** One performed set. */
@Serializable
data class SetLog(
    val id: String = newId(),
    val exerciseId: String,
    val epochDay: Long,
    val timestamp: Long,
    val weightKg: Double,
    val reps: Int,
)

@Serializable
data class AppData(
    val version: Int = 1,
    val exercises: List<Exercise> = emptyList(),
    val plan: List<PlanItem> = emptyList(),
    val logs: List<SetLog> = emptyList(),
)

val MUSCLE_GROUPS = listOf("Brust", "Rücken", "Beine", "Schultern", "Arme", "Bauch", "Ganzkörper", "Cardio")
val EXERCISE_EMOJIS = listOf("🏋️", "💪", "🦵", "🔥", "⚡", "🚀", "🧗", "🤸", "🏃", "🚴", "🥊", "🥇", "🦍", "🐺")
