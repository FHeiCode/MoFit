package com.fheicode.mofit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatTest {
    @Test
    fun formatKg() {
        assertEquals("40", formatKg(40.0))
        assertEquals("32,5", formatKg(32.5))
        assertEquals("1,25", formatKg(1.25))
        assertEquals("0", formatKg(0.0))
    }

    @Test
    fun parseDecimal_acceptsCommaAndDot() {
        assertEquals(32.5, parseDecimal("32,5")!!, 0.0)
        assertEquals(32.5, parseDecimal(" 32.5 ")!!, 0.0)
        assertNull(parseDecimal(""))
        assertNull(parseDecimal("abc"))
    }

    @Test
    fun labels() {
        assertEquals("40 kg × 10", setLabel(40.0, 10))
        assertEquals("12 Wdh", setLabel(0.0, 12))
        assertEquals("3 Sätze · 32,5 kg", planLabel(3, 32.5))
        assertEquals("3 Sätze", planLabel(3, 0.0))
        assertEquals("1 Satz", planLabel(1, 0.0))
    }
}
