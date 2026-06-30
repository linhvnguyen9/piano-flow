package com.linh.pianoflow.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default OS font scale at/above which fixed horizontal layouts (equal-width columns,
 * single-line rows of controls) start to clip and should reflow to a vertical stack.
 */
const val ExpandedFontScaleThreshold = 1.3f

/**
 * True when the OS font scale is at/above [threshold] — the accessibility text size where
 * fixed horizontal layouts start to truncate. Use this one shared predicate rather than
 * hand-coding `LocalDensity.current.fontScale >= …` per screen, so the "what counts as a
 * large font scale" decision lives in exactly one place.
 */
@Composable
fun isExpandedFontScale(threshold: Float = ExpandedFontScaleThreshold): Boolean =
    LocalDensity.current.fontScale >= threshold

/**
 * Lays [items] out as a [Row] of equal-weight cells, but **stacks them vertically**
 * (full-width [Column]) once the OS font scale reaches [stackAtFontScale]. Fixed equal
 * columns clip their content at large accessibility font sizes (a mono `"85%"` → `"85"`);
 * stacking restores full width per item, so the load-bearing value shows in full.
 *
 * Each item is a composable that applies the supplied [Modifier] to its root — `weight(1f)`
 * in the row layout, `fillMaxWidth()` when stacked:
 * ```
 * AdaptiveRow(items = listOf(
 *     { m -> StatCard("Median", median, m) },
 *     { m -> StatCard("Accuracy", accuracy, m) },
 * ))
 * ```
 *
 * This is the repo's standard large-font reflow for "a row of equal cells" — reach for it
 * instead of duplicating a `fontScale >=` branch per screen.
 */
@Composable
fun AdaptiveRow(
    items: List<@Composable (Modifier) -> Unit>,
    modifier: Modifier = Modifier,
    spacing: Dp = 14.dp,
    stackAtFontScale: Float = ExpandedFontScaleThreshold,
) {
    if (isExpandedFontScale(stackAtFontScale)) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
            items.forEach { item -> item(Modifier.fillMaxWidth()) }
        }
    } else {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            items.forEach { item -> item(Modifier.weight(1f)) }
        }
    }
}
