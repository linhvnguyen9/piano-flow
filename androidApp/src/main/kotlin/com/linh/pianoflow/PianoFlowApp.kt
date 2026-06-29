package com.linh.pianoflow

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.linh.pianoflow.feature.chordsmoother.impl.di.chordSmootherModule
import com.linh.pianoflow.feature.sightreading.impl.di.sightReadingModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PianoFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // App-wide Preferences DataStore — owns the on-disk file location. Features consume
        // the DataStore<Preferences> via Koin and namespace their own keys.
        val settingsDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { applicationContext.preferencesDataStoreFile("pianoflow_settings") },
        )

        startKoin {
            modules(
                module { single<DataStore<Preferences>> { settingsDataStore } },
                chordSmootherModule,
                sightReadingModule,
            )
        }
        PianoFlowComponent.installNavigationController(this)
    }
}
