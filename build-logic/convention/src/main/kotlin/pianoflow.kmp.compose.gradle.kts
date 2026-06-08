import com.linh.pianoflow.buildlogic.pianoflowNamespace
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    androidLibrary {
        namespace = moduleNamespace
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(lib("compose-uiToolingPreview"))
        }
        commonMain.dependencies {
            implementation(lib("compose-runtime"))
            implementation(lib("compose-foundation"))
            implementation(lib("compose-material3"))
            implementation(lib("compose-material-icons-core"))
            implementation(lib("compose-ui"))
            implementation(lib("compose-components-resources"))
            implementation(lib("compose-uiToolingPreview"))
            implementation(lib("androidx-lifecycle-viewmodelCompose"))
            implementation(lib("androidx-lifecycle-runtimeCompose"))
        }
        commonTest.dependencies {
            implementation(lib("kotlin-test"))
        }
    }
}
