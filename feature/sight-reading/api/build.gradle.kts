plugins {
    alias(libs.plugins.pianoflow.kmpComposeApi)
    alias(libs.plugins.pianoflow.enro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
        }
    }
}
