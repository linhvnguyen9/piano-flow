plugins {
    alias(libs.plugins.pianoflow.kmpCompose)
    alias(libs.plugins.pianoflow.composeScreenshotTesting)
    alias(libs.plugins.pianoflow.showkase)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)   // PianoKeyboard uses Pitch
        }
    }
}
