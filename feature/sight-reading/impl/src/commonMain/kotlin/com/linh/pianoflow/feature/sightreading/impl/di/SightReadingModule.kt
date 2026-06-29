package com.linh.pianoflow.feature.sightreading.impl.di

import com.linh.pianoflow.feature.sightreading.impl.data.SightReadingSettingsDataStore
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSettings
import com.linh.pianoflow.feature.sightreading.impl.presentation.SightReadingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sightReadingModule = module {
    // The DataStore<Preferences> itself is provided app-side (it owns the file location).
    singleOf(::SightReadingSettingsDataStore) bind SightReadingSettings::class
    viewModelOf(::SightReadingViewModel)
}
