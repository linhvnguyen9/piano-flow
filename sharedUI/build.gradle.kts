plugins {
    id("pianoflow.kmp.compose")
    id("pianoflow.compose-screenshot-testing")
}

kotlin {
    androidLibrary {
        namespace = "com.linh.pianoflow.sharedUI"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            implementation(projects.core.model)
            implementation(projects.core.audio)
            implementation(projects.core.designsystem)
        }
    }
}
