package com.makn.footballquiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val LightColors = lightColorScheme(
    primary = PitchGreen,
    onPrimary = Color.White,
    primaryContainer = PitchGreenContainer,
    onPrimaryContainer = OnPitchGreenContainer,
    secondary = InkMuted,
    onSecondary = Color.White,
    secondaryContainer = PitchGreenContainer,
    onSecondaryContainer = OnPitchGreenContainer,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperContainer,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Paper,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHigh,
    outline = InkMuted,
    outlineVariant = Hairline,
    error = IncorrectRed,
    errorContainer = IncorrectRedContainer,
)

private val DarkColors = darkColorScheme(
    primary = PitchGreenDark,
    onPrimary = Color.Black,
    primaryContainer = PitchGreenContainerDark,
    onPrimaryContainer = OnPitchGreenContainerDark,
    secondary = ChalkMuted,
    onSecondary = Color.Black,
    secondaryContainer = PitchGreenContainerDark,
    onSecondaryContainer = OnPitchGreenContainerDark,
    background = Night,
    onBackground = Chalk,
    surface = Night,
    onSurface = Chalk,
    surfaceVariant = NightContainer,
    onSurfaceVariant = ChalkMuted,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Night,
    surfaceContainer = NightContainer,
    surfaceContainerHigh = NightContainerHigh,
    surfaceContainerHighest = NightContainerHigh,
    outline = ChalkMuted,
    outlineVariant = HairlineDark,
    error = Color(0xFFFFB4AB),
)

private val BaseTypography = Typography()

/** Default Material type scale with slightly heavier headings for a crisp, minimal look. */
private val AppTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
fun FootballQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
