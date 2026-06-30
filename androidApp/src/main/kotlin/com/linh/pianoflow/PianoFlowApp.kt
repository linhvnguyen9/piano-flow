package com.linh.pianoflow

import android.app.Application
import com.linh.pianoflow.feature.chordsmoother.impl.di.chordSmootherModule
import com.linh.pianoflow.feature.sightreading.impl.di.sightReadingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PianoFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PianoFlowApp)
            modules(chordSmootherModule, sightReadingModule)
        }
        PianoFlowComponent.installNavigationController(this)
    }
}
