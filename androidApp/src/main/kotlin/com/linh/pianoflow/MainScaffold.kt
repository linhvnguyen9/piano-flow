package com.linh.pianoflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * of the app uses. The bar is flat (no M3 pill indicator) with a hairline top divider.
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
                            icon = { Icon(entry.icon, contentDescription = null) },
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

private enum class HomeTab(val label: String, val icon: ImageVector) {
    Songs("Songs", Icons.AutoMirrored.Filled.QueueMusic),
    SightReading("Sight Reading", Icons.Filled.MusicNote),
}
