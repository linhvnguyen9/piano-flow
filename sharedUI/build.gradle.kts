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
            api(projects.sharedLogic)
        }
    }
}
