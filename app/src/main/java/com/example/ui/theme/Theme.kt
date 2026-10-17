package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme: Black Background, Crisp White Elements & Text, Light Green Accents
private val DarkColorScheme = darkColorScheme(
  primary = LightGreen,
  onPrimary = PureBlack,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = LightGreen,
  secondary = PureWhite,
  onSecondary = PureBlack,
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = PureWhite,
  tertiary = LightGreenSoft,
  onTertiary = PureBlack,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  error = LightGreen,
  onError = PureBlack
)

// Light Theme: Pure White Background, White Surfaces, Dark Charcoal/Black Elements & Text, Light Green Accents
private val LightColorScheme = lightColorScheme(
  primary = LightGreen,
  onPrimary = PureBlack,
  primaryContainer = Color(0xFFECFDF5),
  onPrimaryContainer = PureBlack,
  secondary = PureBlack,
  onSecondary = PureWhite,
  secondaryContainer = Color(0xFFF3F4F6),
  onSecondaryContainer = PureBlack,
  tertiary = LightGreenDark,
  onTertiary = PureWhite,
  background = PureWhite,
  onBackground = PureBlack,
  surface = PureWhite,
  onSurface = PureBlack,
  surfaceVariant = Color(0xFFF9FAFB),
  onSurfaceVariant = Color(0xFF4B5563),
  outline = Color(0xFFE5E7EB),
  error = Color(0xFFDC2626),
  onError = PureWhite
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = !darkTheme
          isAppearanceLightNavigationBars = !darkTheme
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
