package com.fheicode.mofit.data

/** Aggregated values of one exercise on one training day. */
data class DayStat(
    val epochDay: Long,
    val maxWeight: Double,
    val maxReps: Int,
    val volume: Double,
    val sets: Int,
)

object Stats {

    fun dailyStats(logs: List<SetLog>, exerciseId: String): List<DayStat> =
        logs.asSequence()
            .filter { it.exerciseId == exerciseId }
            .groupBy { it.epochDay }
            .map { (day, sets) ->
                DayStat(
                    epochDay = day,
                    maxWeight = sets.maxOf { it.weightKg },
                    maxReps = sets.maxOf { it.reps },
                    volume = sets.sumOf { it.weightKg * it.reps },
                    sets = sets.size,
                )
            }
            .sortedBy { it.epochDay }

    private fun bestSetComparator(bodyweight: Boolean): Comparator<SetLog> =
        if (bodyweight) compareBy<SetLog>({ it.reps }, { it.weightKg })
        else compareBy<SetLog>({ it.weightKg }, { it.reps })

    /** Best set: heaviest (then most reps); for bodyweight exercises most reps (then heaviest). */
    fun bestSet(logs: List<SetLog>, exercise: Exercise): SetLog? =
        logs.filter { it.exerciseId == exercise.id }.maxWithOrNull(bestSetComparator(exercise.bodyweight))

    /** True if the new set would beat the previous best set. The very first set is not counted as a record. */
    fun isNewRecord(logs: List<SetLog>, exercise: Exercise, weightKg: Double, reps: Int): Boolean {
        val best = bestSet(logs, exercise) ?: return false
        val candidate = best.copy(weightKg = weightKg, reps = reps)
        return bestSetComparator(exercise.bodyweight).compare(candidate, best) > 0
    }

    /** Epley formula: estimated weight for a single repetition. */
    fun estimatedOneRepMax(weightKg: Double, reps: Int): Double = when {
        reps <= 0 -> 0.0
        reps == 1 -> weightKg
        else -> weightKg * (1 + reps / 30.0)
    }

    /** Index of the Monday-based week that contains the given day. (Epoch day 4 = Monday 1970-01-05.) */
    fun weekIndex(epochDay: Long): Long = Math.floorDiv(epochDay - 4, 7L)

    /**
     * Number of consecutive weeks with at least one logged set, ending this week.
     * A week without training so far does not break the streak until it is over.
     */
    fun weekStreak(logs: List<SetLog>, todayEpochDay: Long): Int {
        val weeks = logs.map { weekIndex(it.epochDay) }.toSet()
        var week = weekIndex(todayEpochDay)
        if (week !in weeks) week -= 1
        var streak = 0
        while (week in weeks) {
            streak++
            week--
        }
        return streak
    }
}
