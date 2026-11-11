package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = SkyBluePrimary,
  onPrimary = Color.White,
  primaryContainer = SoftBlueAccent,
  onPrimaryContainer = DeepNavyText,
  secondary = SkyBlueDark,
  onSecondary = Color.White,
  secondaryContainer = VeryLightBlueBackground,
  onSecondaryContainer = DeepNavyText,
  tertiary = WarmAmberAccent,
  onTertiary = Color.White,
  background = MainBackgroundWhite,
  onBackground = DeepNavyText,
  surface = MainBackgroundWhite,
  onSurface = DeepNavyText,
  surfaceVariant = VeryLightBlueBackground,
  onSurfaceVariant = NavyTextSecondary,
  outline = CardBorderSubtle
)

private val DarkColorScheme = darkColorScheme(
  primary = SkyBluePrimary,
  onPrimary = Color.White,
  primaryContainer = SkyBlueDark,
  onPrimaryContainer = VeryLightBlueBackground,
  secondary = SkyBlueLight,
  onSecondary = DeepNavyText,
  background = Color(0xFF141F29),
  onBackground = VeryLightBlueBackground,
  surface = Color(0xFF1B2937),
  onSurface = VeryLightBlueBackground,
  surfaceVariant = Color(0xFF243647),
  onSurfaceVariant = SoftBlueAccent,
  outline = Color(0xFF334B61)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
