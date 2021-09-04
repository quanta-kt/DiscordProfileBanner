package utils

import java.util.*

/**
 * Convert Java Optional to Kotlin nullable
 */
fun <T> Optional<T>.getOrNull(): T? = orElse(null)
