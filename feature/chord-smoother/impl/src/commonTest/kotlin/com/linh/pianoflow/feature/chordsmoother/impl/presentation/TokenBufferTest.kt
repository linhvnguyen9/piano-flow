package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenBufferTest {

    @Test
    fun emptyBuffer_yieldsNothing() {
        assertEquals(emptyList<String>() to "", consumeTokens(""))
    }

    @Test
    fun noDelimiter_keepsEverythingPending() {
        assertEquals(emptyList<String>() to "Cmaj", consumeTokens("Cmaj"))
    }

    @Test
    fun trailingSpace_commitsToken() {
        assertEquals(listOf("Am") to "", consumeTokens("Am "))
    }

    @Test
    fun multipleTokens_lastStaysPendingWithoutTrailingDelimiter() {
        assertEquals(listOf("C", "G") to "Am", consumeTokens("C G Am"))
    }

    @Test
    fun pipesAndCommas_areDelimiters() {
        assertEquals(listOf("C", "G") to "", consumeTokens("C | G | "))
        assertEquals(listOf("C", "G") to "Am", consumeTokens("C,G,Am"))
    }
}
