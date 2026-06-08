plugins {
    alias(libs.plugins.pianoflow.kmpLibrary)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SharedLogic"   // unchanged so ContentView.swift's import still resolves
            isStatic = true
            export(projects.core.model)
            export(projects.core.audio)
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.audio)
        }
    }
}
