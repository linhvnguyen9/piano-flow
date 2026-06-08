package com.linh.pianoflow.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Brand / fixed tokens (mode-independent) -------------------------------
internal val PrimaryFixed = Color(0xFFC2EAEA)
internal val PrimaryFixedDim = Color(0xFFA6CECE)
internal val OnPrimaryFixed = Color(0xFF002020)
internal val OnPrimaryFixedVariant = Color(0xFF264D4D)
internal val SecondaryFixed = Color(0xFFFFDCBD)
internal val SecondaryFixedDim = Color(0xFFF0BD8B)
internal val OnSecondaryFixed = Color(0xFF2C1600)
internal val OnSecondaryFixedVariant = Color(0xFF623F18)
internal val TertiaryFixed = Color(0xFFFFDBCE)
internal val TertiaryFixedDim = Color(0xFFF0BAA6)
internal val OnTertiaryFixed = Color(0xFF301307)
internal val OnTertiaryFixedVariant = Color(0xFF633D2E)

val SereneLightColorScheme = lightColorScheme(
    primary = Color(0xFF325858),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF4A7070),
    onPrimaryContainer = Color(0xFFC9F2F2),
    inversePrimary = Color(0xFFA6CECE),
    secondary = Color(0xFF7D562D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFCA98),
    onSecondaryContainer = Color(0xFF7A532A),
    tertiary = Color(0xFF6F4738),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF8A5F4E),
    onTertiaryContainer = Color(0xFFFFE4DB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF9F9F8),
    onBackground = Color(0xFF1A1C1C),
    surface = Color(0xFFF9F9F8),
    onSurface = Color(0xFF1A1C1C),
    surfaceVariant = Color(0xFFE2E2E2),
    onSurfaceVariant = Color(0xFF414848),
    surfaceTint = Color(0xFF3F6565),
    inverseSurface = Color(0xFF2F3131),
    inverseOnSurface = Color(0xFFF1F1F0),
    outline = Color(0xFF717978),
    outlineVariant = Color(0xFFC0C8C7),
    surfaceBright = Color(0xFFF9F9F8),
    surfaceDim = Color(0xFFDADAD9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F4F3),
    surfaceContainer = Color(0xFFEEEEED),
    surfaceContainerHigh = Color(0xFFE8E8E7),
    surfaceContainerHighest = Color(0xFFE2E2E2),
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

val SereneDarkColorScheme = darkColorScheme(
    primary = PrimaryFixedDim,
    onPrimary = OnPrimaryFixed,
    primaryContainer = OnPrimaryFixedVariant,
    onPrimaryContainer = PrimaryFixed,
    inversePrimary = Color(0xFF325858),
    secondary = SecondaryFixedDim,
    onSecondary = OnSecondaryFixed,
    secondaryContainer = OnSecondaryFixedVariant,
    onSecondaryContainer = SecondaryFixed,
    tertiary = TertiaryFixedDim,
    onTertiary = OnTertiaryFixed,
    tertiaryContainer = OnTertiaryFixedVariant,
    onTertiaryContainer = TertiaryFixed,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1A),
    onBackground = Color(0xFFE8E6E4),
    surface = Color(0xFF1C1B1A),
    onSurface = Color(0xFFE8E6E4),
    surfaceVariant = Color(0xFF49453F),
    onSurfaceVariant = Color(0xFFCBC6C2),
    surfaceTint = PrimaryFixedDim,
    inverseSurface = Color(0xFFE8E6E4),
    inverseOnSurface = Color(0xFF2F3131),
    outline = Color(0xFF948F8B),
    outlineVariant = Color(0xFF49453F),
    surfaceBright = Color(0xFF393634),
    surfaceDim = Color(0xFF141312),
    surfaceContainerLowest = Color(0xFF0F0E0D),
    surfaceContainerLow = Color(0xFF242322),
    surfaceContainer = Color(0xFF282625),
    surfaceContainerHigh = Color(0xFF333130),
    surfaceContainerHighest = Color(0xFF3E3B3A),
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
)

// --- Piano key palette -----------------------------------------------------
// A piano is white-and-black in either app theme, so the base key fills are
// theme-independent (DESIGN.md: white = "off-white fill", black = "dark ink").
// Only the *active* highlight is theme-driven (primary), so a lit note pops in
// both modes.
/** Off-white fill for natural (white) keys — constant across light/dark. */
val PianoWhiteKey = Color(0xFFF9F9F8)
/** "Dark Ink" fill for black piano keys — constant across light/dark. */
val PianoBlackKey = Color(0xFF1C1C1C)
/** Dark-ink label text drawn on white keys — constant across light/dark. */
val PianoKeyLabel = Color(0xFF414848)
/** Subtle edge stroke that defines white-key boundaries in both modes. */
val PianoKeyOutline = Color(0xFFC0C8C7)
