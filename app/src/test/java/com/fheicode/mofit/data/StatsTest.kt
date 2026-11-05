package com.fheicode.mofit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsTest {
    private val bench = Exercise(id = "bench", name = "Bankdrücken", muscleGroup = "Brust", emoji = "🏋️", colorIndex = 0)
    private val pullup = Exercise(id = "pull", name = "Klimmzüge", muscleGroup = "Rücken", emoji = "🧗", colorIndex = 1, bodyweight = true)

    private fun log(ex: Exercise, day: Long, weight: Double, reps: Int) =
        SetLog(exerciseId = ex.id, epochDay = day, timestamp = day, weightKg = weight, reps = reps)

    @Test
    fun dailyStats_groupsByDayAndSorts() {
        val logs = listOf(
            log(bench, 11, 40.0, 8),
            log(bench, 10, 30.0, 10),
            log(bench, 11, 42.5, 6),
            log(pullup, 11, 0.0, 5),
        )
        val stats = Stats.dailyStats(logs, bench.id)
        assertEquals(listOf(10L, 11L), stats.map { it.epochDay })
        assertEquals(42.5, stats[1].maxWeight, 0.0)
        assertEquals(8, stats[1].maxReps)
        assertEquals(40.0 * 8 + 42.5 * 6, stats[1].volume, 0.001)
        assertEquals(2, stats[1].sets)
    }

    @Test
    fun bestSet_weightFirstForWeightedExercises() {
        val logs = listOf(log(bench, 1, 40.0, 10), log(bench, 2, 45.0, 3), log(bench, 3, 45.0, 5))
        val best = Stats.bestSet(logs, bench)!!
        assertEquals(45.0, best.weightKg, 0.0)
        assertEquals(5, best.reps)
    }

    @Test
    fun bestSet_repsFirstForBodyweight() {
        val logs = listOf(log(pullup, 1, 5.0, 4), log(pullup, 2, 0.0, 9))
        assertEquals(9, Stats.bestSet(logs, pullup)!!.reps)
    }

    @Test
    fun isNewRecord() {
        val logs = listOf(log(bench, 1, 40.0, 8))
        assertTrue(Stats.isNewRecord(logs, bench, 42.5, 5))
        assertTrue(Stats.isNewRecord(logs, bench, 40.0, 9))
        assertFalse(Stats.isNewRecord(logs, bench, 40.0, 8))
        assertFalse(Stats.isNewRecord(logs, bench, 35.0, 20))
        assertFalse("first set is no record", Stats.isNewRecord(emptyList(), bench, 50.0, 5))
    }

    @Test
    fun estimatedOneRepMax() {
        assertEquals(0.0, Stats.estimatedOneRepMax(50.0, 0), 0.0)
        assertEquals(50.0, Stats.estimatedOneRepMax(50.0, 1), 0.0)
        assertEquals(60.0, Stats.estimatedOneRepMax(50.0, 6), 0.001)
    }

    @Test
    fun weekIndex_startsOnMonday() {
        val monday = LocalDate.of(2026, 11, 2).toEpochDay()
        val sunday = LocalDate.of(2026, 11, 8).toEpochDay()
        val nextMonday = LocalDate.of(2026, 11, 9).toEpochDay()
        assertEquals(Stats.weekIndex(monday), Stats.weekIndex(sunday))
        assertEquals(Stats.weekIndex(monday) + 1, Stats.weekIndex(nextMonday))
    }

    @Test
    fun weekStreak() {
        val today = LocalDate.of(2026, 11, 5).toEpochDay() // Thursday
        val lastWeek = today - 7
        val twoWeeksAgo = today - 14
        val fourWeeksAgo = today - 28

        assertEquals(0, Stats.weekStreak(emptyList(), today))
        // Nothing yet this week, but last two weeks trained: streak still alive.
        assertEquals(2, Stats.weekStreak(listOf(log(bench, lastWeek, 1.0, 1), log(bench, twoWeeksAgo, 1.0, 1)), today))
        // This week + last week, gap before.
        assertEquals(2, Stats.weekStreak(listOf(log(bench, today, 1.0, 1), log(bench, lastWeek, 1.0, 1), log(bench, fourWeeksAgo, 1.0, 1)), today))
    }
}
