package com.linh.pianoflow.feature.sightreading.impl.testing

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import java.io.File
import java.time.Instant

/**
 * Tier-1 deterministic UI assertions (mobile-design rubric, structural blockers) —
 * the sight-reading module's copy of the same host-test helper used in chord-smoother.
 * Each module emits its own gitignored `_findings.jsonl` sidecar so `aggregate_ledger.py`
 * can fold them into the durable ledger.
 *
 * Walks the unmerged semantics tree and reports — in one shot — every node that violates
 * a structural rule a machine can check without vision: zero-size text, sub-48dp touch
 * targets, horizontal out-of-bounds, and `noTruncate`-tagged text overflow.
 */
object Tier1Assertions {

    private const val MIN_TOUCH_DP = 48f
    private const val MIN_TOUCH_BUTTON_DP = 32f
    private const val NO_TRUNCATE_TAG = "noTruncate"

    // Interactive piano keys are intentionally narrower than 48dp (a 2-octave keyboard
    // can't fit 48dp keys on a phone); the mitigation is the full-height hit area, and the
    // Sight Reading settings sheet surfaces the trade-off. Keys carry `PianoKeyTestTag`
    // (= this value) so the touch-target check skips them instead of flagging every key.
    private const val PIANO_KEY_TAG = "pianoKey"
    private const val OOB_TOLERANCE_PX = 0.5f

    fun assertAll(rule: ComposeContentTestRule, label: String = "") {
        val root = rule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val rootBounds = root.boundsInRoot
        val allNodes = mutableListOf<SemanticsNode>().also { collect(root, it) }

        val violations = mutableListOf<String>()
        val density = rule.density

        for (node in allNodes) {
            val bounds = node.boundsInRoot
            val widthDp = with(density) { bounds.width.toDp().value }
            val heightDp = with(density) { bounds.height.toDp().value }

            if (bounds.width == 0f && bounds.height == 0f) continue

            val hasText = node.config.getOrNull(SemanticsProperties.Text)?.isNotEmpty() == true
            val hasCd = node.config.getOrNull(SemanticsProperties.ContentDescription)?.isNotEmpty() == true
            val hasClick = node.config.getOrNull(SemanticsActions.OnClick) != null

            if ((hasText || hasCd) && (bounds.width == 0f || bounds.height == 0f)) {
                violations += "[zero-size] ${describe(node)}: ${fmt(widthDp)}x${fmt(heightDp)}dp"
            }

            val role = node.config.getOrNull(SemanticsProperties.Role)
            val m3Toggle = role == Role.Switch || role == Role.Checkbox || role == Role.RadioButton
            val pianoKey = node.config.getOrNull(SemanticsProperties.TestTag) == PIANO_KEY_TAG
            val threshold = if (role == Role.Button) MIN_TOUCH_BUTTON_DP else MIN_TOUCH_DP
            if (hasClick && !m3Toggle && !pianoKey && (widthDp < threshold || heightDp < threshold)) {
                if (!hasLargeClickableAncestor(node, density)) {
                    violations += "[touch-target] ${describe(node)}: ${fmt(widthDp)}x${fmt(heightDp)}dp (< ${fmt(threshold)}x${fmt(threshold)}dp)"
                }
            }

            if (bounds.right > rootBounds.right + OOB_TOLERANCE_PX ||
                bounds.left < rootBounds.left - OOB_TOLERANCE_PX
            ) {
                violations += "[out-of-bounds] ${describe(node)}: " +
                    "x=${fmt(bounds.left)}..${fmt(bounds.right)} root=${fmt(rootBounds.left)}..${fmt(rootBounds.right)}"
            }

            val tag = node.config.getOrNull(SemanticsProperties.TestTag)
            if (tag == NO_TRUNCATE_TAG && hasText && textOverflowed(node)) {
                violations += "[text-overflow] ${describe(node)} (tagged $NO_TRUNCATE_TAG)"
            }
        }

        if (violations.isNotEmpty()) {
            writeFindings(label, violations)

            val header = if (label.isEmpty()) "Tier-1 violations" else "Tier-1 violations in $label"
            throw AssertionError(
                "$header (${violations.size}):\n  - " + violations.joinToString("\n  - "),
            )
        }
    }

    private fun writeFindings(label: String, violations: List<String>) {
        val (screen, config) = parseLabel(label)
        val ts = Instant.now().toString()
        val sidecar = File("build/outputs/roborazzi/_findings.jsonl")
        sidecar.parentFile?.mkdirs()
        val sb = StringBuilder()
        for (v in violations) {
            val category = categoryOf(v)
            sb.append("""{"ts":"$ts","screen":"${esc(screen)}","config":"${esc(config)}",""")
            sb.append(""""loop":"impl","role":"tier1","tier":1,""")
            sb.append(""""category":"$category","severity":"blocker",""")
            sb.append(""""element":"${esc(elementOf(v))}","finding":"${esc(v)}",""")
            sb.append(""""fix":null,"iteration":1,"resolved":false,"deposit":null}""")
            sb.append('\n')
        }
        synchronized(LEDGER_LOCK) { sidecar.appendText(sb.toString()) }
    }

    private fun parseLabel(label: String): Pair<String, String> {
        val core = label.removePrefix("_inspect_").removeSuffix(".png")
        val sep = core.indexOf('_')
        return if (sep > 0) core.substring(0, sep) to core.substring(sep + 1)
        else core to ""
    }

    private fun categoryOf(finding: String): String = when {
        finding.startsWith("[zero-size]") -> "zero-size"
        finding.startsWith("[touch-target]") -> "touch-target"
        finding.startsWith("[out-of-bounds]") -> "out-of-bounds"
        finding.startsWith("[text-overflow]") -> "truncation"
        else -> "unknown"
    }

    private fun elementOf(finding: String): String {
        val afterCategory = finding.substringAfter("] ", finding)
        return afterCategory.substringBefore(":").trim()
    }

    private fun esc(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private val LEDGER_LOCK = Any()

    private fun hasLargeClickableAncestor(
        node: SemanticsNode,
        density: androidx.compose.ui.unit.Density,
    ): Boolean {
        var p = node.parent
        while (p != null) {
            if (p.config.getOrNull(SemanticsActions.OnClick) != null) {
                val w = with(density) { p.boundsInRoot.width.toDp().value }
                val h = with(density) { p.boundsInRoot.height.toDp().value }
                if (w >= MIN_TOUCH_DP && h >= MIN_TOUCH_DP) return true
            }
            p = p.parent
        }
        return false
    }

    private fun collect(node: SemanticsNode, out: MutableList<SemanticsNode>) {
        out += node
        node.children.forEach { collect(it, out) }
    }

    private fun textOverflowed(node: SemanticsNode): Boolean {
        val getLayout = node.config.getOrNull(SemanticsActions.GetTextLayoutResult) ?: return false
        val results = mutableListOf<TextLayoutResult>()
        getLayout.action?.invoke(results)
        return results.any { it.hasVisualOverflow }
    }

    private fun describe(node: SemanticsNode): String {
        val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" / ") { it.text }
        val cd = node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" / ")
        val tag = node.config.getOrNull(SemanticsProperties.TestTag)
        val role = node.config.getOrNull(SemanticsProperties.Role)?.toString()
        val parts = listOfNotNull(
            tag?.let { "tag=$it" },
            role?.let { "role=$it" },
            text?.takeIf { it.isNotBlank() }?.let { "text=\"${it.take(40)}\"" },
            cd?.takeIf { it.isNotBlank() }?.let { "cd=\"${it.take(40)}\"" },
        )
        return if (parts.isEmpty()) "node#${node.id}" else parts.joinToString(" ")
    }

    private fun fmt(v: Float): String = "%.1f".format(v)
}
