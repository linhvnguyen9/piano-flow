package com.linh.pianoflow.feature.sightreading.impl.domain

import kotlinx.coroutines.flow.Flow

/** The persisted learner-aid preferences for the Sight Reading trainer. */
data class SightReadingPreferences(
    val showNoteNames: Boolean = true,
    val showMiddleC: Boolean = true,
)

/**
 * Port for reading + persisting [SightReadingPreferences]. The presentation layer depends
 * on this abstraction; the DataStore-backed adapter lives in `data/`. [preferences] emits
 * the current value and every subsequent change, so a freshly-created ViewModel re-seeds
 * from disk on launch (or after a tab switch).
 */
interface SightReadingSettings {
    val preferences: Flow<SightReadingPreferences>
    suspend fun setShowNoteNames(value: Boolean)
    suspend fun setShowMiddleC(value: Boolean)
}
