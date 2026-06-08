package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.runtime.Composable
import com.linh.pianoflow.feature.chordsmoother.api.SongsKey
import dev.enro.annotations.NavigationDestination

@Composable
@NavigationDestination(SongsKey::class)
fun SongsDestination() {
    SongsScreen()
}
