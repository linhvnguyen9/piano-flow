package com.linh.pianoflow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform