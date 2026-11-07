package com.fheicode.mofit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MuscleGroupTest {
    private fun ex(name: String, group: String) =
        Exercise(name = name, muscleGroup = group, emoji = "💪", colorIndex = 0)

    @Test
    fun groupsListUpdated() {
        assertFalse("Ganzkörper" in MUSCLE_GROUPS)
        assertFalse("Arme" in MUSCLE_GROUPS)
        listOf("Bizeps", "Trizeps", "Unterarm").forEach { assert(it in MUSCLE_GROUPS) }
    }

    @Test
    fun oldArmeIsSplitByName() {
        assertEquals("Bizeps", migrateMuscleGroup(ex("Bizeps-Curls", "Arme")).muscleGroup)
        assertEquals("Trizeps", migrateMuscleGroup(ex("Trizeps Pushdown", "Arme")).muscleGroup)
        assertEquals("Trizeps", migrateMuscleGroup(ex("Dips", "Arme")).muscleGroup)
        assertEquals("Unterarm", migrateMuscleGroup(ex("Unterarm-Curls", "Arme")).muscleGroup)
        assertEquals("Bizeps", migrateMuscleGroup(ex("Hammer Curls", "Arme")).muscleGroup)
    }

    @Test
    fun otherGroupsUntouched() {
        assertEquals("Brust", migrateMuscleGroup(ex("Bankdrücken", "Brust")).muscleGroup)
        assertEquals("Ganzkörper", migrateMuscleGroup(ex("Burpees", "Ganzkörper")).muscleGroup)
    }
}
