plugins {
    id("pianoflow.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.linh.pianoflow.sharedLogic"
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
}
