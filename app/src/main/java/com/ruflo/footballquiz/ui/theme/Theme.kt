package com.ruflo.footballquiz.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = PitchGreen,
    onPrimary = Color.White,
    primaryContainer = CorrectGreenContainer,
    secondary = FloodlightGold,
    onSecondary = Color.Black,
    tertiary = SkyBlue,
    onTertiary = Color.White,
    error = IncorrectRed,
    errorContainer = IncorrectRedContainer,
)

private val DarkColors = darkColorScheme(
    primary = PitchGreenDark,
    onPrimary = Color.Black,
    secondary = FloodlightGoldDark,
    onSecondary = Color.Black,
    tertiary = SkyBlueDark,
    onTertiary = Color.Black,
    error = Color(0xFFEF9A9A),
)

@Composable
fun FootballQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
