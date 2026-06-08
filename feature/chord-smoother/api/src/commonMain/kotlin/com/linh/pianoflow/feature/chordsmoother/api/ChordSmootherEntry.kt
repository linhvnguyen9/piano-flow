package com.linh.pianoflow.feature.chordsmoother.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ChordSmootherEntry {
    @Composable
    fun Content(modifier: Modifier = Modifier)
}
