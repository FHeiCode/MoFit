package com.fheicode.mofit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Dark "gym at night" palette with neon accents. */
object MoFitColors {
    val Background = Color(0xFF0B0B12)
    val Surface = Color(0xFF15151F)
    val SurfaceHigh = Color(0xFF1F1F2C)
    val Outline = Color(0xFF2E2E40)

    val Lime = Color(0xFFC6FF3D)
    val Pink = Color(0xFFFF3D8B)
    val Violet = Color(0xFF7C5CFF)
    val Orange = Color(0xFFFF8A3D)
    val Cyan = Color(0xFF3DDCFF)
    val Yellow = Color(0xFFFFD43D)
    val Mint = Color(0xFF3DFFA8)
    val Red = Color(0xFFFF5C5C)

    val TextPrimary = Color(0xFFF4F4FA)
    val TextSecondary = Color(0xFF9A9AB0)

    val HeroGradient = Brush.linearGradient(listOf(Violet, Pink, Orange))
    val LimeGradient = Brush.linearGradient(listOf(Lime, Mint))

    val Palette = listOf(Lime, Pink, Violet, Orange, Cyan, Yellow, Mint, Red)

    fun palette(index: Int): Color = Palette[Math.floorMod(index, Palette.size)]
}

private val ColorScheme = darkColorScheme(
    primary = MoFitColors.Lime,
    onPrimary = Color.Black,
    primaryContainer = MoFitColors.Lime.copy(alpha = 0.18f),
    onPrimaryContainer = MoFitColors.Lime,
    secondary = MoFitColors.Pink,
    onSecondary = Color.White,
    secondaryContainer = MoFitColors.SurfaceHigh,
    onSecondaryContainer = MoFitColors.TextPrimary,
    tertiary = MoFitColors.Violet,
    onTertiary = Color.White,
    background = MoFitColors.Background,
    onBackground = MoFitColors.TextPrimary,
    surface = MoFitColors.Surface,
    onSurface = MoFitColors.TextPrimary,
    surfaceVariant = MoFitColors.SurfaceHigh,
    onSurfaceVariant = MoFitColors.TextSecondary,
    surfaceContainerLowest = MoFitColors.Background,
    surfaceContainerLow = MoFitColors.Surface,
    surfaceContainer = MoFitColors.Surface,
    surfaceContainerHigh = MoFitColors.SurfaceHigh,
    surfaceContainerHighest = MoFitColors.SurfaceHigh,
    outline = MoFitColors.Outline,
    outlineVariant = MoFitColors.Outline,
    error = MoFitColors.Red,
)

private val BaseTypography = Typography()

private val MoFitTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp),
    displayMedium = BaseTypography.displayMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = BaseTypography.bodyLarge,
    bodyMedium = BaseTypography.bodyMedium,
    bodySmall = BaseTypography.bodySmall,
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
    labelMedium = BaseTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = BaseTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
)

@Composable
fun MoFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = MoFitTypography, content = content)
}
