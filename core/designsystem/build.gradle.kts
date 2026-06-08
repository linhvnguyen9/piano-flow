plugins {
    id("pianoflow.kmp.compose")
    id("pianoflow.compose-screenshot-testing")
}

kotlin {
    androidLibrary {
        namespace = "com.linh.pianoflow.core.designsystem"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)   // PianoKeyboard uses Pitch
        }
    }
}
