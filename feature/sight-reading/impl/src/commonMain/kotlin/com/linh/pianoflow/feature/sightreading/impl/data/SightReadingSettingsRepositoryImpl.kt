package com.linh.pianoflow.feature.sightreading.impl.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingPreferences
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [SightReadingSettingsRepository] backed by a Preferences [DataStore]. The store (with its
 * platform file location) is provided via DI by the module's platform Koin module — this
 * adapter only knows the keys. Missing keys fall back to the learner-friendly defaults
 * (both aids on).
 */
class SightReadingSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SightReadingSettingsRepository {

    override val preferences: Flow<SightReadingPreferences> =
        dataStore.data.map { prefs ->
            SightReadingPreferences(
                showNoteNames = prefs[Keys.SHOW_NOTE_NAMES] ?: true,
                showMiddleC = prefs[Keys.SHOW_MIDDLE_C] ?: true,
            )
        }

    override suspend fun setShowNoteNames(value: Boolean) {
        dataStore.edit { it[Keys.SHOW_NOTE_NAMES] = value }
    }

    override suspend fun setShowMiddleC(value: Boolean) {
        dataStore.edit { it[Keys.SHOW_MIDDLE_C] = value }
    }

    private object Keys {
        val SHOW_NOTE_NAMES = booleanPreferencesKey("sight_reading_show_note_names")
        val SHOW_MIDDLE_C = booleanPreferencesKey("sight_reading_show_middle_c")
    }
}
