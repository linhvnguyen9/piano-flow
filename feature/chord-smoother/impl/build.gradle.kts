plugins {
    alias(libs.plugins.pianoflow.kmpCompose)
    alias(libs.plugins.pianoflow.composeScreenshotTesting)
    alias(libs.plugins.pianoflow.enro)
    alias(libs.plugins.pianoflow.koin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.chordSmoother.api)
            implementation(projects.core.model)
            implementation(projects.core.audio)
            implementation(projects.core.designsystem)
        }
    }
}
