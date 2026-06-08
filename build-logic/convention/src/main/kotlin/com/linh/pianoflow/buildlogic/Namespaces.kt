package com.linh.pianoflow.buildlogic

import org.gradle.api.Project

fun Project.pianoflowNamespace(): String =
    "com.linh.pianoflow." + path.removePrefix(":").replace(":", ".").replace("-", "")
