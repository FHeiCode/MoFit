package com.fheicode.mofit.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartScaleTest {

    private fun assertCovers(ticks: List<Double>, min: Double, max: Double) {
        assertTrue("$ticks should start at or below $min", ticks.first() <= min)
        assertTrue("$ticks should end at or above $max", ticks.last() >= max)
    }

    @Test
    fun smallWeightIncrease_isNotStretchedOverTheWholeHeight() {
        val t = ChartScale.ticks(30.0, 32.5, integer = false)
        assertCovers(t, 30.0, 32.5)
        // 2.5 kg must be well under half of the axis span.
        assertTrue((t.last() - t.first()) >= 4 * 2.5)
    }

    @Test
    fun ticksAreEvenRoundSteps() {
        val t = ChartScale.ticks(20.0, 60.0, integer = false)
        assertEquals(listOf(20.0, 40.0, 60.0), t)
        val steps = t.zipWithNext { a, b -> b - a }.distinct()
        assertEquals(1, steps.size)
    }

    @Test
    fun singleValue_hasRoomAboveAndBelow() {
        val t = ChartScale.ticks(30.0, 30.0, integer = false)
        assertTrue(t.first() < 30.0 && t.last() > 30.0)
    }

    @Test
    fun repsUseWholeNumbers() {
        val t = ChartScale.ticks(9.0, 10.0, integer = true)
        assertCovers(t, 9.0, 10.0)
        assertTrue(t.all { it % 1.0 == 0.0 })
    }

    @Test
    fun neverBelowZero() {
        assertTrue(ChartScale.ticks(0.0, 0.0, integer = false).first() >= 0.0)
        assertTrue(ChartScale.ticks(0.0, 5.0, integer = false).first() >= 0.0)
    }

    @Test
    fun xPositions_followTheCalendar() {
        // Training on day 0, day 2 and after a long break on day 20.
        val x = ChartScale.xFractions(listOf(100L, 102L, 120L))
        assertEquals(0f, x[0], 1e-6f)
        assertEquals(0.1f, x[1], 1e-6f)
        assertEquals(1f, x[2], 1e-6f)
        assertEquals(listOf(0.5f), ChartScale.xFractions(listOf(5L)))
    }
}
