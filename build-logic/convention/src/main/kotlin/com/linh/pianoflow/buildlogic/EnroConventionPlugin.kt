package com.linh.pianoflow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class EnroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()

        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        val enro = lib("enro")
        val enroProcessor = lib("enro-processor")
        val serializationCore = lib("kotlinx-serialization-core")

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.named("commonMain") {
                    dependencies {
                        api(enro)
                        implementation(serializationCore)
                    }
                }
            }
            dependencies.add("kspCommonMainMetadata", enroProcessor)
            dependencies.add("kspAndroid", enroProcessor)
        }

        pluginManager.withPlugin("com.android.application") {
            dependencies.add("implementation", enro)
            dependencies.add("ksp", enroProcessor)
        }
    }
}
