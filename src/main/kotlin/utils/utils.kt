package utils

import java.util.*

fun <T> Optional<T>.getOrNull(): T? = orElse(null)