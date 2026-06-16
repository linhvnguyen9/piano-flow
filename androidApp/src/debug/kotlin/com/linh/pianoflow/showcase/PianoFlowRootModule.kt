package com.linh.pianoflow.showcase

import com.airbnb.android.showkase.annotation.ShowkaseRoot
import com.airbnb.android.showkase.annotation.ShowkaseRootModule

/**
 * Aggregates every @ShowkaseComposable across the app's module dependencies into the
 * generated `Showkase` object. Debug-only: the browser and generated metadata never ship.
 */
@ShowkaseRoot
class PianoFlowRootModule : ShowkaseRootModule
