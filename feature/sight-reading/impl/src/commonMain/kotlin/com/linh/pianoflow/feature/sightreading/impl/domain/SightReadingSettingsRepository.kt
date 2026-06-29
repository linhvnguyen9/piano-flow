package com.linh.pianoflow.feature.sightreading.impl.domain

import kotlinx.coroutines.flow.Flow

/** The persisted learner-aid preferences for the Sight Reading trainer. */
data class SightReadingPreferences(
    val showNoteNames: Boolean = true,
    val showMiddleC: Boolean = true,
)

/**
 * Repository for reading + persisting [SightReadingPreferences]. The presentation layer
 * depends on this abstraction; the DataStore-backed `SightReadingSettingsRepositoryImpl`
 * lives in `data/`. [preferences] emits the current value and every later change, so a
 * freshly-created ViewModel re-seeds from disk on launch (or after a tab switch).
 */
interface SightReadingSettingsRepository {
    val preferences: Flow<SightReadingPreferences>
    suspend fun setShowNoteNames(value: Boolean)
    suspend fun setShowMiddleC(value: Boolean)
}
