package com.linh.pianoflow.feature.sightreading.impl.presentation

import androidx.compose.runtime.Composable
import com.linh.pianoflow.feature.sightreading.api.SightReadingKey
import dev.enro.annotations.NavigationDestination

@Composable
@NavigationDestination(SightReadingKey::class)
fun SightReadingDestination() {
    SightReadingScreen()
}
