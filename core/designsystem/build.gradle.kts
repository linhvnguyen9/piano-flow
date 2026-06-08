plugins {
    alias(libs.plugins.pianoflow.kmpCompose)
    alias(libs.plugins.pianoflow.composeScreenshotTesting)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)   // PianoKeyboard uses Pitch
        }
    }
}
