import com.linh.pianoflow.buildlogic.pianoflowNamespace
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention for a feature **api** module: a KMP + Android + iOS library (like
 * [pianoflow.kmp.library]) plus just enough Compose to declare the public `@Composable`
 * entry seam — `compose-runtime` + `compose-ui` (Modifier), and the Compose compiler. No
 * Compose UI widgets (material3/foundation) belong in an api module, so they are
 * deliberately left out; reach for [pianoflow.kmp.compose] in impl modules instead.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")
fun lib(alias: String) = libs.findLibrary(alias).get()
val moduleNamespace = pianoflowNamespace()

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        namespace = moduleNamespace
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(lib("compose-runtime"))
            implementation(lib("compose-ui"))
        }
        commonTest.dependencies {
            implementation(lib("kotlin-test"))
        }
    }
}
