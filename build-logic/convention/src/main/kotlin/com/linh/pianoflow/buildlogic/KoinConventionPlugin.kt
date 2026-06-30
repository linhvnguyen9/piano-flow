package com.linh.pianoflow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Adds the Koin DI dependencies (BOM + core + compose + viewmodel) to a KMP module's
 * `commonMain`, so feature modules don't repeat the four-line block. Modelled on
 * [EnroConventionPlugin]; reacts to the Kotlin Multiplatform plugin so application order
 * doesn't matter.
 *
 * Module-specific persistence/networking deps (e.g. DataStore) stay in the module — only
 * the cross-cutting DI wiring lives here.
 */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain") {
                    dependencies {
                        implementation(target.dependencies.platform(lib("koin-bom")))
                        implementation(lib("koin-core"))
                        implementation(lib("koin-compose"))
                        implementation(lib("koin-compose-viewmodel"))
                    }
                }
            }
        }
    }
}
