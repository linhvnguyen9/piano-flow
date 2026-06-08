plugins {
    id("pianoflow.kmp.compose")
    id("pianoflow.compose-screenshot-testing")
}

kotlin {
    androidLibrary {
        namespace = "com.linh.pianoflow.feature.chordsmoother.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.api.chordSmoother)
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
