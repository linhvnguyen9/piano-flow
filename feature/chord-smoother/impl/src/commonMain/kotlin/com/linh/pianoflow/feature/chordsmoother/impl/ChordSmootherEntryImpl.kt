package com.linh.pianoflow.feature.chordsmoother.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linh.pianoflow.feature.chordsmoother.api.ChordSmootherEntry
import com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsScreen

class ChordSmootherEntryImpl : ChordSmootherEntry {
    @Composable
    override fun Content(modifier: Modifier) {
        SongsScreen(modifier = modifier)
    }
}
