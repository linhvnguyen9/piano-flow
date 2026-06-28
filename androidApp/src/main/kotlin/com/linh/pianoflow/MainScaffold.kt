package com.linh.pianoflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import com.linh.pianoflow.feature.chordsmoother.api.SongsKey
import com.linh.pianoflow.feature.sightreading.api.SightReadingKey

/**
 * App shell: a two-tab bottom navigation (Songs · Sight Reading) over the feature
 * destinations. Each tab hosts its own Enro [rememberNavigationContainer] rooted at the
 * feature's [NavigationKey], so tabs render through the same destination wiring the rest
 * of the app uses. The bar is flat (no M3 pill indicator) with a hairline top divider,
 * matching the design.
 */
@Composable
fun MainScaffold() {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Songs) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                ) {
                    HomeTab.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = tab == entry,
                            onClick = { tab = entry },
                            icon = { entry.Icon() },
                            label = {
                                Text(
                                    entry.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (tab == entry) FontWeight.Bold else FontWeight.SemiBold,
                                    ),
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                HomeTab.Songs -> TabHost(SongsKey)
                HomeTab.SightReading -> TabHost(SightReadingKey)
            }
        }
    }
}

@Composable
private fun TabHost(key: NavigationKey) {
    val container = rememberNavigationContainer(backstack = backstackOf(key.asInstance()))
    NavigationDisplay(state = container)
}

private enum class HomeTab(val label: String) {
    Songs("Songs"),
    SightReading("Sight Reading");

    @Composable
    fun Icon() = when (this) {
        Songs -> SongsNavIcon()
        SightReading -> SightReadingNavIcon()
    }
}

@Composable
private fun SongsNavIcon() {
    val tint = LocalContentColor.current
    Canvas(Modifier.size(24.dp)) {
        val u = size.minDimension / 24f
        val stem = Path().apply {
            moveTo(9 * u, 18 * u)
            lineTo(9 * u, 6 * u)
            lineTo(19 * u, 4 * u)
            lineTo(19 * u, 16 * u)
        }
        drawPath(stem, color = tint, style = Stroke(width = 2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(tint, radius = 2.5f * u, center = Offset(6.5f * u, 18 * u))
        drawCircle(tint, radius = 2.5f * u, center = Offset(16.5f * u, 16 * u))
    }
}

@Composable
private fun SightReadingNavIcon() {
    val tint = LocalContentColor.current
    Canvas(Modifier.size(24.dp)) {
        val u = size.minDimension / 24f
        val sw = 1.8f * u
        drawLine(tint, Offset(4 * u, 7 * u), Offset(20 * u, 7 * u), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(4 * u, 11 * u), Offset(20 * u, 11 * u), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(4 * u, 15 * u), Offset(20 * u, 15 * u), strokeWidth = sw, cap = StrokeCap.Round)
        drawOval(tint, topLeft = Offset((13f - 3.2f) * u, (15f - 2.4f) * u), size = Size(6.4f * u, 4.8f * u))
    }
}
