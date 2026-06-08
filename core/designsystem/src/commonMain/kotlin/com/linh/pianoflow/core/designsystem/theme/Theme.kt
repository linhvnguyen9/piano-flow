package com.linh.pianoflow.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun PianoFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SereneDarkColorScheme else SereneLightColorScheme
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalExtendedTypography provides pianoFlowExtendedTypography(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = pianoFlowTypography(),
            shapes = PianoFlowShapes,
            content = content,
        )
    }
}

object PianoFlowTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val extendedTypography: ExtendedTypography
        @Composable @ReadOnlyComposable get() = LocalExtendedTypography.current
}
