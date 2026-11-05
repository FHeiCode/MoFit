package com.fheicode.mofit.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val DAY_SHORT = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
val DAY_FULL = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")

/** 40.0 -> "40", 32.5 -> "32,5", 1.25 -> "1,25" */
fun formatKg(value: Double): String {
    if (value % 1.0 == 0.0) return value.toLong().toString()
    return String.format(Locale.GERMAN, "%.2f", value).trimEnd('0').trimEnd(',')
}

fun parseDecimal(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

/** "40 kg × 10" or, without weight, "10 Wdh". */
fun setLabel(weightKg: Double, reps: Int): String =
    if (weightKg <= 0.0) "$reps Wdh" else "${formatKg(weightKg)} kg × $reps"

fun planLabel(sets: Int, reps: Int, weightKg: Double): String =
    if (weightKg <= 0.0) "$sets × $reps Wdh" else "$sets × $reps · ${formatKg(weightKg)} kg"

private val longDate = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.GERMAN)
private val historyDate = DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", Locale.GERMAN)
private val shortDate = DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMAN)

fun formatLongDate(date: LocalDate): String = date.format(longDate)
fun formatHistoryDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(historyDate)
fun formatShortDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortDate)
