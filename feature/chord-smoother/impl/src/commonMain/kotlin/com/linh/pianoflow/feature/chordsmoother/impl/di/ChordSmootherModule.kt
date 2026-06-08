package com.linh.pianoflow.feature.chordsmoother.impl.di

import com.linh.pianoflow.audio.TonePlayer
import com.linh.pianoflow.feature.chordsmoother.api.ChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.api.ProgressionSolver
import com.linh.pianoflow.feature.chordsmoother.impl.domain.DefaultChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.impl.domain.DefaultProgressionSolver
import com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chordSmootherModule = module {
    singleOf(::DefaultProgressionSolver) bind ProgressionSolver::class
    singleOf(::DefaultChordProgressionParser) bind ChordProgressionParser::class
    single { TonePlayer() }
    viewModelOf(::SongsViewModel)
}
