package com.linh.pianoflow.showcase

import com.airbnb.android.showkase.models.Showkase
import org.junit.Assert.assertTrue
import org.junit.Test

class ShowkaseMetadataTest {

    @Test
    fun metadata_contains_catalogComponents() {
        val names = Showkase.getMetadata().componentList.map { it.componentName }
        assertTrue("Expected 'PianoKeyboard' in $names", "PianoKeyboard" in names)
        assertTrue("Expected 'ChordProgressionField' in $names", "ChordProgressionField" in names)
    }
}
