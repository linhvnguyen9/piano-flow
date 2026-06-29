package com.linh.pianoflow.feature.sightreading.impl.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates the feature's Preferences [DataStore], located under the Android app's files dir.
 * Requires `androidContext(...)` to be set on the Koin application (the app does this in
 * `startKoin`).
 */
actual val sightReadingPlatformModule: Module = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("sight_reading_settings") },
        )
    }
}
