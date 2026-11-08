package com.fheicode.mofit.data

/** One set to be logged. */
data class SetEntry(val exerciseId: String, val weightKg: Double, val reps: Int)

/**
 * "Plan übernehmen": instead of entering every set by hand, the still-open sets of a
 * plan entry are logged in one go with the planned weight. The plan has no reps, so
 * reps come from the last logged set of that exercise (or 10 the very first time).
 */
object PlanAdoption {
    const val DEFAULT_REPS = 10

    fun repsFor(exerciseId: String, logs: List<SetLog>): Int =
        logs.filter { it.exerciseId == exerciseId }.maxByOrNull { it.timestamp }?.reps ?: DEFAULT_REPS

    fun openSets(item: PlanItem, logs: List<SetLog>, todayEpochDay: Long): Int {
        val done = logs.count { it.exerciseId == item.exerciseId && it.epochDay == todayEpochDay }
        return (item.sets - done).coerceAtLeast(0)
    }

    /** The sets needed to complete the given plan entries today. */
    fun entriesFor(items: List<PlanItem>, logs: List<SetLog>, todayEpochDay: Long): List<SetEntry> {
        val result = mutableListOf<SetEntry>()
        // Count sets added for earlier entries, in case an exercise appears twice in a day.
        val pending = logs.toMutableList()
        for (item in items) {
            val open = openSets(item, pending, todayEpochDay)
            val reps = repsFor(item.exerciseId, logs)
            repeat(open) {
                result += SetEntry(item.exerciseId, item.weightKg, reps)
                pending += SetLog(exerciseId = item.exerciseId, epochDay = todayEpochDay, timestamp = 0, weightKg = item.weightKg, reps = reps)
            }
        }
        return result
    }
}
