package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = ForestGreenPrimary,
  onPrimary = Color.White,
  primaryContainer = ForestGreenContainer,
  onPrimaryContainer = OnForestGreenContainer,
  secondary = ForestAccent,
  onSecondary = Color.White,
  secondaryContainer = LightSagePill,
  onSecondaryContainer = ForestGreenDark,
  tertiary = WarmGoldAccent,
  onTertiary = Color.White,
  background = SoftCreamBackground,
  onBackground = TextPrimaryDark,
  surface = SoftCreamSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = SoftCreamBorder,
  onSurfaceVariant = TextSecondaryDark,
  outline = SoftCreamBorder,
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF67B788),
  onPrimary = ForestGreenDark,
  primaryContainer = ForestGreenLight,
  onPrimaryContainer = Color.White,
  secondary = Color(0xFF86CCA0),
  onSecondary = ForestGreenDark,
  background = ForestGreenDark,
  onBackground = SoftCreamBackground,
  surface = Color(0xFF132A1E),
  onSurface = SoftCreamBackground,
  surfaceVariant = Color(0xFF1C3A2B),
  onSurfaceVariant = Color(0xFFC2D4C8),
  outline = Color(0xFF2E533F),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamicColor to false by default so DWELLO HOMES brand colors (Dark Forest Green & Soft Cream) are always visible
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
