package com.linh.pianoflow.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PianoFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Full-pill radius for buttons and chips. */
val Pill = RoundedCornerShape(percent = 50)

/** Distinctive 24dp top-only "drawer" radius for bottom sheets. */
val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
