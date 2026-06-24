package com.linh.pianoflow.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/**
 * Konsist module-boundary rules, enforced as ordinary JVM unit tests.
 *
 * **Why Konsist, not Detekt.** These are architecture invariants over imports, and
 * Konsist parses source straight from disk with no Gradle plugin — so it sidesteps
 * the two traps that make Detekt awkward in this build: KMP source-set wiring, and
 * the AGP-9 plugin-compat lag on `com.android.kotlin.multiplatform.library` modules
 * (see CLAUDE.md "Build basics"). It is also the same "rule as a host test" shape the
 * harness already uses in `Tier1Assertions` — one prevention mechanism, not two.
 *
 * These enforce *module boundaries* — the durable kind of architecture invariant:
 * `core` never depends on a `feature`, and a feature's public `api` never leaks its
 * `impl`. Each is caught the moment a forbidding import is written, before any build
 * wiring or review notices.
 *
 * Run just these:
 * `./gradlew :androidApp:testDebugUnitTest --tests "com.linh.pianoflow.architecture.ArchitectureTest"`
 */
class ArchitectureTest {

    private val files: List<KoFileDeclaration> = Konsist.scopeFromProject().files

    /** Core modules are foundational — they must never depend on a feature. */
    @Test
    fun `core does not depend on feature`() {
        files
            .filter { it.isProductionSource() && it.modulePath().startsWith("core/") }
            .assertFalse { file ->
                file.hasImport { it.name.startsWith("com.linh.pianoflow.feature.") }
            }
    }

    /** A feature's public `api` contract must not leak its own `impl` internals. */
    @Test
    fun `feature api does not depend on feature impl`() {
        files
            .filter {
                it.isProductionSource() &&
                    it.modulePath().startsWith("feature/") &&
                    it.modulePath().endsWith("/api")
            }
            .assertFalse { file ->
                file.hasImport {
                    it.name.startsWith("com.linh.pianoflow.feature.") && it.name.contains(".impl.")
                }
            }
    }

}

/** Source-set folder (the path segment after `src/`); null for build/generated or non-source files. */
private fun KoFileDeclaration.sourceSet(): String? {
    val segs = projectPath.trimStart('/').split('/')
    if ("build" in segs) return null
    val i = segs.indexOf("src")
    return if (i >= 0) segs.getOrNull(i + 1) else null
}

/** True for main/production source sets; excludes `test`, `commonTest`, `androidHostTest`, etc. */
private fun KoFileDeclaration.isProductionSource(): Boolean {
    val ss = sourceSet() ?: return false
    return ss != "test" && !ss.endsWith("Test")
}

/** Gradle module path = everything before `/src/`, e.g. `feature/chord-smoother/impl`. */
private fun KoFileDeclaration.modulePath(): String =
    projectPath.trimStart('/').substringBefore("/src/")
