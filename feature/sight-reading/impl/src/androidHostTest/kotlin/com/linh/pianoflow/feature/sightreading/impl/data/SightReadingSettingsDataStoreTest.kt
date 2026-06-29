package com.linh.pianoflow.feature.sightreading.impl.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

/**
 * Round-trips the DataStore-backed settings: defaults when nothing is stored, and each
 * toggle persists independently and reads back. Uses a throwaway temp file per test
 * instance (JUnit news the class per @Test, so the file/store are isolated).
 */
class SightReadingSettingsDataStoreTest {

    private val file: File = File.createTempFile("sr_settings", ".preferences_pb").also { it.delete() }
    private val store = SightReadingSettingsDataStore(
        PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() }),
    )

    @After
    fun cleanup() {
        file.delete()
    }

    @Test
    fun defaults_to_both_aids_on() = runBlocking {
        assertEquals(
            SightReadingPreferences(showNoteNames = true, showMiddleC = true),
            store.preferences.first(),
        )
    }

    @Test
    fun persists_each_toggle_independently() = runBlocking {
        store.setShowNoteNames(false)
        assertEquals(
            SightReadingPreferences(showNoteNames = false, showMiddleC = true),
            store.preferences.first(),
        )

        store.setShowMiddleC(false)
        assertEquals(
            SightReadingPreferences(showNoteNames = false, showMiddleC = false),
            store.preferences.first(),
        )

        store.setShowNoteNames(true)
        assertEquals(
            SightReadingPreferences(showNoteNames = true, showMiddleC = false),
            store.preferences.first(),
        )
    }
}
