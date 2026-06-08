package com.linh.pianoflow

import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule

@NavigationComponent
object PianoFlowComponent : NavigationComponentConfiguration(
    module = createNavigationModule { },
)
