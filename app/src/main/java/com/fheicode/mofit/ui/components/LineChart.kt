package com.fheicode.mofit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.ui.theme.MoFitColors

/** Minimal neon line chart: glowing line, gradient fill, dots. */
@Composable
fun LineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val min = values.min()
        val max = values.max()
        val padV = 14.dp.toPx()
        val padH = 8.dp.toPx()
        val chartH = size.height - 2 * padV
        val chartW = size.width - 2 * padH

        val points = values.mapIndexed { i, v ->
            val x = if (values.size == 1) size.width / 2 else padH + chartW * i / (values.size - 1)
            val y = if (max == min) padV + chartH / 2 else padV + chartH - (v - min) / (max - min) * chartH
            Offset(x, y)
        }

        // Subtle horizontal guide lines
        for (i in 0..2) {
            val y = padV + chartH * i / 2
            drawLine(MoFitColors.Outline, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        if (points.size > 1) {
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
            // glow
            drawPath(line, color.copy(alpha = 0.25f), style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        points.forEachIndexed { i, p ->
            val last = i == points.lastIndex
            val r = if (last) 6.dp.toPx() else 4.dp.toPx()
            drawCircle(MoFitColors.Background, radius = r, center = p)
            drawCircle(color, radius = r, center = p, style = Stroke(width = 2.5.dp.toPx()))
            if (last) drawCircle(color, radius = r / 2, center = p)
        }
    }
}
