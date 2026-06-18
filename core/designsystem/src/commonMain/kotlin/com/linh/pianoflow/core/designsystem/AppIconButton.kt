package com.linh.pianoflow.core.designsystem

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Design-system icon button with a guaranteed 48 × 48 dp visual + tap target.
 *
 * Material 3's [IconButton] defaults to a 40 × 40 dp visual surface (extending the
 * tap area to 48 dp via `minimumInteractiveComponentSize` reserved padding). That
 * extension is invisible to the semantics tree, so a Tier-1 touch-target check
 * sees the inner 40 dp and treats it as a violation. Forcing the visual to 48 dp
 * here unifies "what the user sees" and "what we assert" — and is the right call
 * for this app's dense rows where the icons sit immediately next to other
 * controls (the M3 default's tap-padding can collide with neighbours).
 *
 * Prefer this over `androidx.compose.material3.IconButton` in feature code.
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        colors = colors,
        content = content,
    )
}
