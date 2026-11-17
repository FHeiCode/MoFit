package com.fheicode.mofit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanAdoptionTest {
    private val today = 20_000L

    private fun log(exerciseId: String, day: Long, weight: Double, reps: Int, ts: Long) =
        SetLog(exerciseId = exerciseId, epochDay = day, timestamp = ts, weightKg = weight, reps = reps)

    private fun plan(exerciseId: String, sets: Int, weight: Double) =
        PlanItem(dayOfWeek = 1, exerciseId = exerciseId, sets = sets, weightKg = weight)

    @Test
    fun firstTime_usesPlanWeightAndDefaultReps() {
        val entries = PlanAdoption.entriesFor(listOf(plan("bench", 3, 30.0)), emptyList(), today)
        assertEquals(3, entries.size)
        assertTrue(entries.all { it.exerciseId == "bench" && it.weightKg == 30.0 && it.reps == PlanAdoption.DEFAULT_REPS })
    }

    @Test
    fun repsComeFromLastLoggedSet() {
        val logs = listOf(log("bench", today - 7, 27.5, 8, ts = 1), log("bench", today - 2, 30.0, 12, ts = 2))
        val entries = PlanAdoption.entriesFor(listOf(plan("bench", 2, 32.5)), logs, today)
        assertEquals(listOf(12, 12), entries.map { it.reps })
        assertEquals(listOf(32.5, 32.5), entries.map { it.weightKg })
    }

    @Test
    fun onlyOpenSetsAreAdded() {
        val logs = listOf(log("bench", today, 30.0, 10, ts = 1))
        assertEquals(2, PlanAdoption.entriesFor(listOf(plan("bench", 3, 30.0)), logs, today).size)

        val done = List(3) { log("bench", today, 30.0, 10, ts = it.toLong()) }
        assertEquals(0, PlanAdoption.entriesFor(listOf(plan("bench", 3, 30.0)), done, today).size)
    }

    @Test
    fun wholePlan_multipleExercises() {
        val items = listOf(plan("bench", 3, 30.0), plan("squat", 2, 40.0), plan("pullup", 3, 0.0))
        val logs = listOf(log("squat", today, 40.0, 8, ts = 1))
        val entries = PlanAdoption.entriesFor(items, logs, today)
        assertEquals(3, entries.count { it.exerciseId == "bench" })
        assertEquals(1, entries.count { it.exerciseId == "squat" })
        assertEquals(3, entries.count { it.exerciseId == "pullup" && it.weightKg == 0.0 })
    }

    @Test
    fun heavierSet_raisesTodaysPlanEntryOnly() {
        val mon = plan("bench", 3, 30.0).copy(dayOfWeek = 1)
        val fri = plan("bench", 3, 32.5).copy(dayOfWeek = 5)
        val raised = PlanAdoption.raisedPlanItem(listOf(mon, fri), "bench", 1, 35.0)!!
        assertEquals(mon.id, raised.id)
        assertEquals(35.0, raised.weightKg, 0.0)
    }

    @Test
    fun lighterOrSameSet_orNotPlannedToday_changesNothing() {
        val mon = plan("bench", 3, 30.0).copy(dayOfWeek = 1)
        assertEquals(null, PlanAdoption.raisedPlanItem(listOf(mon), "bench", 1, 30.0))
        assertEquals(null, PlanAdoption.raisedPlanItem(listOf(mon), "bench", 1, 25.0))
        assertEquals(null, PlanAdoption.raisedPlanItem(listOf(mon), "bench", 3, 40.0))
        assertEquals(null, PlanAdoption.raisedPlanItem(listOf(mon), "squat", 1, 40.0))
    }

    /** The reported problem: + with more weight, then ✓ next week must not go back down. */
    @Test
    fun adoptAfterHeavierSet_keepsTheHigherWeight() {
        val mon = plan("bench", 3, 30.0).copy(dayOfWeek = 1)
        val raised = PlanAdoption.raisedPlanItem(listOf(mon), "bench", 1, 35.0)!!
        val nextWeek = PlanAdoption.entriesFor(listOf(raised), emptyList(), today + 7)
        assertTrue(nextWeek.all { it.weightKg == 35.0 })
    }
}
