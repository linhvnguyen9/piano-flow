package com.linh.pianoflow.feature.sightreading.impl.di

import com.linh.pianoflow.feature.sightreading.impl.data.SightReadingSettingsRepositoryImpl
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSettingsRepository
import com.linh.pianoflow.feature.sightreading.impl.presentation.SightReadingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Platform binding for the `DataStore<Preferences>` the settings repository reads/writes.
 * The file location is platform-specific (Android files dir), so the actual provider lives
 * in `androidMain` — keeping all of this feature's persistence wiring inside the module.
 */
expect val sightReadingPlatformModule: Module

val sightReadingModule = module {
    includes(sightReadingPlatformModule)
    singleOf(::SightReadingSettingsRepositoryImpl) bind SightReadingSettingsRepository::class
    viewModelOf(::SightReadingViewModel)
}
