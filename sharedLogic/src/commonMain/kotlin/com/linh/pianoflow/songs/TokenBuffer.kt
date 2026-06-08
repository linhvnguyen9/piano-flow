package com.linh.pianoflow.songs

private val DELIM = Regex("[|,\\s]+")
private val DELIM_CHAR = Regex("[|,\\s]")

/**
 * Splits a caret buffer into completed tokens plus a trailing pending fragment.
 * A fragment after the last delimiter is "still being typed" and stays pending;
 * if the buffer ends with a delimiter, everything is committed and pending is "".
 */
fun consumeTokens(raw: String): Pair<List<String>, String> {
    if (raw.isEmpty()) return emptyList<String>() to ""
    val nonBlank = raw.split(DELIM).filter { it.isNotBlank() }
    if (nonBlank.isEmpty()) return emptyList<String>() to ""
    val endsWithDelim = DELIM_CHAR.matches(raw.last().toString())
    return if (endsWithDelim) nonBlank to "" else nonBlank.dropLast(1) to nonBlank.last()
}
