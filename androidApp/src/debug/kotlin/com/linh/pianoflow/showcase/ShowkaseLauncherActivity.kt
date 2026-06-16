package com.linh.pianoflow.showcase

import android.app.Activity
import android.os.Bundle
import com.airbnb.android.showkase.models.Showkase

/**
 * Debug-only trampoline: a second launcher icon ("PianoFlow Catalog") that opens the
 * Showkase component browser, then finishes. Keeps the main app UI untouched.
 */
class ShowkaseLauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Showkase.getBrowserIntent(this))
        finish()
    }
}
