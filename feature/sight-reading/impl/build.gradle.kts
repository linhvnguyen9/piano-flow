plugins {
    alias(libs.plugins.pianoflow.kmpCompose)
    alias(libs.plugins.pianoflow.composeScreenshotTesting)
    alias(libs.plugins.pianoflow.enro)
    alias(libs.plugins.pianoflow.koin)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.sightReading.api)
            implementation(projects.core.model)
            implementation(projects.core.audio)
            implementation(projects.core.designsystem)
            implementation(libs.compose.material.icons.extended)
            // Preferences DataStore is this module's own persistence (not shared), so it
            // stays here rather than in a convention plugin. core = common keys/edit API;
            // the Android factory + koin-android (for the Context) live in androidMain.
            implementation(libs.datastore.preferences.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.datastore.preferences)
        }
    }
}
