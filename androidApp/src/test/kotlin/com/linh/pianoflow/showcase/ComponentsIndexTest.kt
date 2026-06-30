package com.linh.pianoflow.showcase

import com.airbnb.android.showkase.models.Showkase
import org.junit.Test
import java.io.File

/**
 * Generates docs/components/COMPONENTS.md from Showkase metadata + a hand-maintained
 * fully-qualified-name/usage map. Run alongside the screenshot recording:
 *   ./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.record=true
 *
 * Add a `details` entry whenever you add a catalog component (a missing entry renders
 * blank cells — a visible, self-correcting gap).
 */
class ComponentsIndexTest {

    /** componentName -> (fully-qualified composable, one-line usage snippet). */
    private val details: Map<String, Pair<String, String>> = mapOf(
        "PianoKeyboard" to (
            "com.linh.pianoflow.core.designsystem.PianoKeyboard" to
                "PianoKeyboard(startMidi = 60, endMidi = 72, highlighted = setOf(60, 64, 67), labels = true)"
        ),
        "MusicStaff" to (
            "com.linh.pianoflow.core.designsystem.MusicStaff" to
                "MusicStaff(midi = 67, state = StaffNoteState.Default, space = 22.dp, width = 240.dp)"
        ),
        "ChordProgressionField" to (
            "com.linh.pianoflow.feature.chordsmoother.impl.presentation.ChordProgressionField" to
                "ChordProgressionField(tokens = listOf(\"Cmaj7\"), editingText = \"\", examples = emptyList(), onTokensChange = {}, onEditingTextChange = {}, onChipTap = {}, onOpenPicker = {}, onPickExample = {})"
        ),
    )

    @Test
    fun generateComponentsIndex() {
        val components = Showkase.getMetadata().componentList.sortedWith(
            compareBy({ it.group }, { it.componentName }),
        )

        val sb = StringBuilder()
        sb.appendLine("# PianoFlow Component Catalog")
        sb.appendLine()
        sb.appendLine("> Generated — do not edit by hand. Regenerate after adding/altering catalog components:")
        sb.appendLine("> `./gradlew :androidApp:testDebugUnitTest -Proborazzi.test.record=true`")
        sb.appendLine(">")
        sb.appendLine("> Browse interactively on a debug build via the **PianoFlow Catalog** launcher icon.")
        sb.appendLine()
        sb.appendLine("| Component | Group | Preview | Composable | Usage |")
        sb.appendLine("|---|---|---|---|---|")
        components.forEach { c ->
            val (fqn, usage) = details[c.componentName] ?: ("" to "")
            val img = "![${c.componentName}](screenshots/${c.group}/${c.componentName}.png)"
            sb.appendLine("| ${c.componentName} | ${c.group} | $img | `$fqn` | `$usage` |")
        }

        // Relative to the androidApp module dir → repo-root/docs/components
        val out = File("../docs/components/COMPONENTS.md")
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
    }
}
