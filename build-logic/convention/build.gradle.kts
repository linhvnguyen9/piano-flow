plugins {
    `kotlin-dsl`
}

group = "com.linh.pianoflow.buildlogic"

dependencies {
    implementation(libs.kotlinMultiplatform.gradlePlugin)
    implementation(libs.androidKmpLibrary.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.composeCompiler.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.kotlinSerialization.gradlePlugin)

    // Available at compile time so the class-based Roborazzi convention plugin can
    // reference the Kotlin Multiplatform and Roborazzi extension types. The plugins
    // themselves are applied (and provided on the classpath) by the consuming build.
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.roborazzi.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("composeScreenshotTesting") {
            id = "pianoflow.compose-screenshot-testing"
            implementationClass =
                "com.linh.pianoflow.buildlogic.ComposeScreenshotTestingConventionPlugin"
        }
        register("enro") {
            id = "pianoflow.enro"
            implementationClass = "com.linh.pianoflow.buildlogic.EnroConventionPlugin"
        }
        register("showkase") {
            id = "pianoflow.showkase"
            implementationClass = "com.linh.pianoflow.buildlogic.ShowkaseConventionPlugin"
        }
    }
}
