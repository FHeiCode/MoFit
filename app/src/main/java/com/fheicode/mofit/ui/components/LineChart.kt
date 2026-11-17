package com.fheicode.mofit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fheicode.mofit.ui.formatKg
import com.fheicode.mofit.ui.formatShortDate
import com.fheicode.mofit.ui.theme.MoFitColors
import kotlin.math.abs

/**
 * Neon line chart with a labelled value axis, points placed by date, and tap-to-inspect.
 * [unit] is shown on the axis and in the tap label; [integer] keeps axis steps whole (reps).
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    color: Color,
    unit: String,
    integer: Boolean,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    var selected by remember(points) { mutableStateOf<Int?>(null) }
    val xs = remember(points) { ChartScale.xFractions(points.map { it.epochDay }) }
    val ticks = remember(points, integer) {
        ChartScale.ticks(points.minOf { it.value }, points.maxOf { it.value }, integer)
    }
    val axisStyle = TextStyle(color = MoFitColors.TextSecondary, fontSize = 11.sp)
    val tipStyle = TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)

    Canvas(
        modifier.pointerInput(points) {
            detectTapGestures { tap ->
                // Pick the point nearest to the tap (horizontally); tap it again to hide.
                val axisW = ticks.maxOf { measurer.measure(formatKg(it), axisStyle).size.width } + 8.dp.toPx()
                val left = axisW + 10.dp.toPx()
                val width = size.width - left - 10.dp.toPx()
                val nearest = xs.indices.minByOrNull { abs(left + xs[it] * width - tap.x) }
                selected = if (nearest == selected) null else nearest
            }
        }
    ) {
        if (points.isEmpty()) return@Canvas
        val labels = ticks.map { measurer.measure(formatKg(it), axisStyle) }
        val axisW = labels.maxOf { it.size.width } + 8.dp.toPx()
        val left = axisW + 10.dp.toPx()
        val right = size.width - 10.dp.toPx()
        val top = 22.dp.toPx() // room for the tap label above the highest point
        val bottom = size.height - 8.dp.toPx()
        val lo = ticks.first()
        val hi = ticks.last()

        fun yOf(v: Double) = (bottom - (v - lo) / (hi - lo) * (bottom - top)).toFloat()
        val offsets = points.mapIndexed { i, p -> Offset(left + xs[i] * (right - left), yOf(p.value)) }

        // Value axis: grid line + label for every tick.
        ticks.forEachIndexed { i, t ->
            val y = yOf(t)
            drawLine(MoFitColors.Outline, Offset(left - 4.dp.toPx(), y), Offset(right, y), strokeWidth = 1.dp.toPx())
            val label = labels[i]
            drawText(label, topLeft = Offset(axisW - label.size.width, y - label.size.height / 2))
        }

        if (offsets.size > 1) {
            val line = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(offsets.last().x, bottom)
                lineTo(offsets.first().x, bottom)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.30f), Color.Transparent), startY = top, endY = bottom))
            drawPath(line, color.copy(alpha = 0.25f), style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        offsets.forEachIndexed { i, p ->
            val emphasized = i == offsets.lastIndex || i == selected
            val r = if (emphasized) 6.dp.toPx() else 4.dp.toPx()
            drawCircle(MoFitColors.Background, radius = r, center = p)
            drawCircle(color, radius = r, center = p, style = Stroke(width = 2.5.dp.toPx()))
            if (emphasized) drawCircle(color, radius = r / 2, center = p)
        }

        selected?.let { i ->
            val p = offsets[i]
            drawLine(
                color.copy(alpha = 0.6f), Offset(p.x, top), Offset(p.x, bottom),
                strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
            val text = measurer.measure("${formatKg(points[i].value)} $unit · ${formatShortDate(points[i].epochDay)}", tipStyle)
            val padX = 8.dp.toPx()
            val padY = 4.dp.toPx()
            val w = text.size.width + 2 * padX
            val h = text.size.height + 2 * padY
            val x = (p.x - w / 2).coerceIn(left, right - w)
            val y = (p.y - h - 10.dp.toPx()).coerceAtLeast(0f)
            drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(8.dp.toPx()))
            drawText(text, topLeft = Offset(x + padX, y + padY))
        }
    }
}
