package com.linh.pianoflow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Wires Airbnb Showkase into a module.
 *
 * - KMP library modules (`org.jetbrains.kotlin.multiplatform`): puts `showkase-annotation`
 *   on the `androidMain` source set (where the `@ShowkaseComposable` wrappers live) and runs
 *   the processor on the Android target via `kspAndroid`.
 * - The application module (`com.android.application`): adds the Showkase browser
 *   (`debugImplementation`), the annotation, and runs the processor on debug via `kspDebug`,
 *   so the `@ShowkaseRoot` aggregator and generated `Showkase` object exist in debug builds only.
 *
 * Mirrors EnroConventionPlugin. Apply after the Kotlin Multiplatform / Android plugins.
 */
class ShowkaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()

        pluginManager.apply("com.google.devtools.ksp")

        val showkase = lib("showkase")
        val showkaseAnnotation = lib("showkase-annotation")
        val showkaseProcessor = lib("showkase-processor")

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    if (name == "androidMain") {
                        dependencies {
                            implementation(showkaseAnnotation)
                        }
                    }
                }
            }
            dependencies.add("kspAndroid", showkaseProcessor)
        }

        pluginManager.withPlugin("com.android.application") {
            dependencies.add("debugImplementation", showkase)
            dependencies.add("implementation", showkaseAnnotation)
            dependencies.add("kspDebug", showkaseProcessor)
        }
    }
}
