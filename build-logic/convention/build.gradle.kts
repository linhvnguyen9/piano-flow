plugins {
    `kotlin-dsl`
}

group = "com.linh.pianoflow.buildlogic"

dependencies {
    // Available at compile time so the convention plugin can reference the Kotlin
    // Multiplatform and Roborazzi extension types. The plugins themselves are applied
    // (and provided on the classpath) by the consuming build.
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
    }
}
