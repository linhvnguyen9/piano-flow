package com.linh.pianoflow

import android.app.Application
import org.koin.core.context.startKoin

class PianoFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules()   // feature modules are added in Phase 4
        }
    }
}
