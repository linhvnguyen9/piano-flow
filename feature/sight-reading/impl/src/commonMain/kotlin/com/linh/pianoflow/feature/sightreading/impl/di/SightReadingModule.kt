package com.linh.pianoflow.feature.sightreading.impl.di

import com.linh.pianoflow.feature.sightreading.impl.presentation.SightReadingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sightReadingModule = module {
    viewModelOf(::SightReadingViewModel)
}
