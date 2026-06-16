package com.linh.pianoflow.showcase

import com.airbnb.android.showkase.models.Showkase
import org.junit.Assert.assertTrue
import org.junit.Test

class ShowkaseMetadataTest {

    @Test
    fun metadata_contains_pianoKeyboard() {
        val names = Showkase.getMetadata().componentList.map { it.componentName }
        assertTrue("Expected 'PianoKeyboard' in Showkase metadata but got $names", "PianoKeyboard" in names)
    }
}
