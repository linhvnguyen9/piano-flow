package com.linh.pianoflow.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Wires Airbnb Showkase into the application module.
 *
 * The catalog's showcase wrappers (`@ShowkaseComposable`) and its `@ShowkaseRoot` aggregator
 * live in `androidApp`'s `debug` source set, so the Showkase browser library and the generated
 * `Showkase` metadata exist in debug builds only and never reach release or the core modules.
 * The processor (`kspDebug`) generates code that references the Showkase runtime models, so the
 * full `showkase` library — not just `showkase-annotation` — must be on the debug classpath.
 *
 * Mirrors EnroConventionPlugin. Apply after the Android application + Compose plugins.
 */
class ShowkaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        fun lib(alias: String) = libs.findLibrary(alias).get()

        pluginManager.apply("com.google.devtools.ksp")

        val showkase = lib("showkase")
        val showkaseProcessor = lib("showkase-processor")

        pluginManager.withPlugin("com.android.application") {
            dependencies.add("debugImplementation", showkase)
            dependencies.add("kspDebug", showkaseProcessor)
        }
    }
}
