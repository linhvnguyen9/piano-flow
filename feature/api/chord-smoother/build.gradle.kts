plugins {
    id("pianoflow.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.linh.pianoflow.feature.chordsmoother.api"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            // Compose runtime + ui only: enough for the @Composable entry seam and
            // Modifier. No Compose UI widgets (material3/foundation) live in :api.
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}
