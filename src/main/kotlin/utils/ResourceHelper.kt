package utils

import java.net.URL

object ResourceHelper {
    fun getResource(path: String): URL? {
        return this::class.java.classLoader.getResource(path)
    }
}