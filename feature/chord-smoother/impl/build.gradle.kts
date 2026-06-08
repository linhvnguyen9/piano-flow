plugins {
    alias(libs.plugins.pianoflow.kmpCompose)
    alias(libs.plugins.pianoflow.composeScreenshotTesting)
    alias(libs.plugins.pianoflow.enro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.chordSmoother.api)
            implementation(projects.core.model)
            implementation(projects.core.audio)
            implementation(projects.core.designsystem)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}
