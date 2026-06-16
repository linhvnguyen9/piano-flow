package com.linh.pianoflow.showcase

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every catalogued component (one Showkase entry per test instance) and writes a
 * committed PNG to docs/components/screenshots/<group>/<name>.png. The wrappers already wrap
 * themselves in PianoFlowTheme + Surface, so we render the entry as-is.
 *
 * Record:  ./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.record=true
 * Verify:  ./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.verify=true
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Use a plain Application, not the manifest's PianoFlowApp — the latter calls startKoin(),
// which throws KoinApplicationAlreadyStartedException across parameterized test instances.
// The stateless showcase wrappers don't need Koin.
@Config(application = Application::class, qualifiers = "w360dp-h640dp-xxhdpi", sdk = [35])
class ShowkaseCatalogScreenshotTest(
    private val component: ShowkaseBrowserComponent,
) {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun capture() {
        composeRule.setContent { component.component() }
        composeRule.onRoot().captureRoboImage(
            // Relative to the androidApp module dir → repo-root/docs/components/screenshots
            filePath = "../docs/components/screenshots/${component.group}/${component.componentName}.png",
        )
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun components(): List<Array<Any>> =
            Showkase.getMetadata().componentList.map { arrayOf(it) }
    }
}
