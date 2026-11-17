package com.fheicode.mofit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fheicode.mofit.data.Exercise
import com.fheicode.mofit.ui.formatKg
import com.fheicode.mofit.ui.parseDecimal
import com.fheicode.mofit.ui.theme.MoFitColors

val CardShape = RoundedCornerShape(24.dp)

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 12.dp, bottom = 8.dp)) {
        if (subtitle != null) {
            Text(
                subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MoFitColors.Lime,
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineLarge)
    }
}

@Composable
fun MoCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MoFitColors.Outline,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(CardShape)
            .background(MoFitColors.Surface)
            .border(1.dp, borderColor, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    brush: Brush = MoFitColors.HeroGradient,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(CardShape)
            .background(brush)
            .padding(20.dp),
        content = content,
    )
}

@Composable
fun ExerciseAvatar(exercise: Exercise?, size: Dp = 46.dp) {
    val color = MoFitColors.palette(exercise?.colorIndex ?: 0)
    val shape = RoundedCornerShape(size * 0.32f)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(color.copy(alpha = 0.16f))
            .border(1.5.dp, color.copy(alpha = 0.7f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(exercise?.emoji ?: "❓", fontSize = (size.value * 0.48f).sp)
    }
}

@Composable
fun StatTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier, accent: Color = MoFitColors.Lime) {
    MoCard(modifier) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = accent, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MoFitColors.TextSecondary, maxLines = 1)
    }
}

/** Row of small pills, filled for completed sets. */
@Composable
fun SetDots(done: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(maxOf(total, done)) { i ->
            val filled = i < done
            val extra = i >= total
            Box(
                Modifier
                    .height(8.dp)
                    .width(if (extra) 10.dp else 22.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            filled && extra -> MoFitColors.Pink
                            filled -> color
                            else -> MoFitColors.SurfaceHigh
                        }
                    )
            )
        }
    }
}

/** − [ value ] + with a directly editable field in the middle. */
@Composable
fun NumberStepper(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double,
    unit: String,
    modifier: Modifier = Modifier,
    min: Double = 0.0,
    decimals: Boolean = false,
    onValidityChange: (Boolean) -> Unit = {},
) {
    // Review fix: the field used to be re-created from `value` on every change, which
    // swallowed a just-typed comma (deleting "32,5" to "32," jumped to "32"), and input
    // that could not be used (empty, "0" reps, "8,5" reps) stayed visible while the old
    // number was saved. Now the typed text is kept, invalid text is marked red and
    // reported, and only the ± buttons or a new value from outside overwrite it.
    var text by remember { mutableStateOf(formatKg(value)) }
    val parsed = parseDecimal(text)
    val valid = parsed != null && parsed >= min && (decimals || parsed % 1.0 == 0.0)
    LaunchedEffect(value) {
        if (parseDecimal(text) != value) text = formatKg(value)
    }
    LaunchedEffect(valid) { onValidityChange(valid) }
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MoFitColors.TextSecondary)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val buttonColors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MoFitColors.SurfaceHigh,
                contentColor = MoFitColors.Lime,
            )
            FilledTonalIconButton(
                onClick = { onValueChange(maxOf(min, (parsed ?: value) - step)) },
                colors = buttonColors,
                modifier = Modifier.size(52.dp),
            ) { Icon(Icons.Rounded.Remove, contentDescription = "weniger") }
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input
                    val number = parseDecimal(input)
                    if (number != null && number >= min && (decimals || number % 1.0 == 0.0)) {
                        onValueChange(number)
                    }
                },
                isError = !valid,
                singleLine = true,
                suffix = { Text(unit, color = MoFitColors.TextSecondary) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MoFitColors.Lime,
                    unfocusedBorderColor = MoFitColors.Outline,
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            FilledTonalIconButton(
                onClick = { onValueChange(maxOf(min, (parsed ?: value) + step)) },
                colors = buttonColors,
                modifier = Modifier.size(52.dp),
            ) { Icon(Icons.Rounded.Add, contentDescription = "mehr") }
        }
    }
}

@Composable
fun EmptyHint(emoji: String, title: String, text: String, modifier: Modifier = Modifier) {
    MoCard(modifier.fillMaxWidth()) {
        Text(emoji, fontSize = 36.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MoFitColors.TextSecondary)
    }
}
