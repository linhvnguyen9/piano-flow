import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.pianoflow.enro)
    alias(libs.plugins.pianoflow.showkase)
    // NOTE: the io.github.takahirom.roborazzi Gradle plugin is intentionally NOT applied here.
    // On AGP 9 it fails to apply to com.android.application (expects the removed legacy
    // TestedExtension). We don't need it: the roborazzi runtime records/verifies off the
    // roborazzi.test.* system properties, which the tasks.withType<Test> block below sets,
    // and the catalog test writes to explicit filePaths.
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.feature.chordSmoother.api)
    implementation(projects.feature.chordSmoother.impl)
    implementation(projects.feature.sightReading.api)
    implementation(projects.feature.sightReading.impl)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.testJunit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    // Provides the androidx.activity.ComponentActivity that createComposeRule() hosts the
    // composition in. Must be debugImplementation (not testImplementation) so its activity
    // merges into the debug manifest Robolectric reads.
    debugImplementation(libs.androidx.compose.ui.testManifest)
}

android {
    namespace = "com.linh.pianoflow"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.linh.pianoflow"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// Mirror ComposeScreenshotTestingConventionPlugin: translate -Proborazzi.test.{record,verify,compare}
// into the system properties Roborazzi reads, and register them as task inputs so switching
// modes invalidates the test cache automatically.
tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    listOf("record", "verify", "compare").forEach { mode ->
        val key = "roborazzi.test.$mode"
        val value = providers.gradleProperty(key).getOrElse("false")
        systemProperty(key, value)
        inputs.property(key, value)
    }
}