plugins {
    id("pianoflow.kmp.compose")
    id("pianoflow.compose-screenshot-testing")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)   // PianoKeyboard uses Pitch
        }
    }
}
