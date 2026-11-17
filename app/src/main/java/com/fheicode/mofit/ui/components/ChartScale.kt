package com.fheicode.mofit.ui.components

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/** One point of the progress curve: a training day and its value. */
data class ChartPoint(val epochDay: Long, val value: Double)

/** Pure axis math for [LineChart], kept separate so it can be unit-tested. */
object ChartScale {

    /**
     * Round axis steps (1, 2, 2.5, 5 × 10ⁿ) that cover [min]..[max].
     * Review fix: the old chart stretched the smallest to the largest value over the
     * full height, so 30 → 32.5 kg looked like doubling. Now the axis has labelled,
     * round steps around the values. A single value gets some room above and below.
     */
    fun ticks(min: Double, max: Double, integer: Boolean, targetCount: Int = 4): List<Double> {
        var lo = min
        var hi = max
        if (hi - lo < 1e-9) {
            val room = max(2.0, abs(hi) * 0.2)
            lo -= room / 2
            hi += room / 2
        } else if (hi - lo < hi * 0.25) {
            // Small changes stay small: the axis spans at least a quarter of the top value.
            lo = hi * 0.75
        }
        lo = max(0.0, lo)

        val raw = (hi - lo) / (targetCount - 1)
        val magnitude = 10.0.pow(floor(log10(raw)))
        var step = listOf(1.0, 2.0, 2.5, 5.0, 10.0).first { it * magnitude >= raw - 1e-9 } * magnitude
        if (integer) step = max(1.0, ceil(step))

        val first = floor(lo / step + 1e-9) * step
        val last = ceil(hi / step - 1e-9) * step
        val count = Math.round((last - first) / step).toInt().coerceAtLeast(1)
        return (0..count).map { round(first + it * step) }
    }

    /**
     * Horizontal position 0..1 by date. Review fix: points used to be spaced evenly by
     * index, so a three-week break looked the same as two days.
     */
    fun xFractions(epochDays: List<Long>): List<Float> {
        if (epochDays.isEmpty()) return emptyList()
        val first = epochDays.first()
        val span = epochDays.last() - first
        if (span == 0L) return epochDays.map { 0.5f }
        return epochDays.map { (it - first).toFloat() / span }
    }

    private fun round(v: Double) = Math.round(v * 1000) / 1000.0
}
