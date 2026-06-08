package com.linh.pianoflow.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pianoflow.core.designsystem.generated.resources.Res
import pianoflow.core.designsystem.generated.resources.ibm_plex_mono_medium
import pianoflow.core.designsystem.generated.resources.ibm_plex_mono_regular
import pianoflow.core.designsystem.generated.resources.plus_jakarta_sans_bold
import pianoflow.core.designsystem.generated.resources.plus_jakarta_sans_regular
import pianoflow.core.designsystem.generated.resources.plus_jakarta_sans_semibold

@Composable
fun plusJakartaSans(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

@Composable
fun ibmPlexMono(): FontFamily = FontFamily(
    Font(Res.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium),
)

@Immutable
data class ExtendedTypography(
    /** Monospace style for notes & chord names (`music-data`). */
    val musicData: TextStyle,
    /** All-caps, wide-tracked micro label (`label-caps`). Uppercase at the call site. */
    val labelCaps: TextStyle,
)

@Composable
fun pianoFlowExtendedTypography(): ExtendedTypography {
    val mono = ibmPlexMono()
    val sans = plusJakartaSans()
    return ExtendedTypography(
        musicData = TextStyle(
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.05.em,
        ),
        labelCaps = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.em,
        ),
    )
}

val LocalExtendedTypography = staticCompositionLocalOf {
    // Non-themed fallback (used only if something renders outside PianoFlowTheme).
    ExtendedTypography(
        musicData = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.05.em,
        ),
        labelCaps = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.em,
        ),
    )
}

@Composable
fun pianoFlowTypography(): Typography {
    val sans = plusJakartaSans()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.Bold,
            fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.02).em,
        ),
        displayMedium = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.Bold,
            fontSize = 34.sp, lineHeight = 42.sp, letterSpacing = (-0.02).em,
        ),
        displaySmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp, lineHeight = 38.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp, lineHeight = 40.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp, lineHeight = 36.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, lineHeight = 28.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.Normal,
            fontSize = 18.sp, lineHeight = 28.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = sans, fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
        ),
    )
}
