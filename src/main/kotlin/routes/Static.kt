package routes

import io.ktor.http.content.*
import io.ktor.routing.*

fun Routing.staticRoutes() {
    static("static") {
        resources("static")
    }
}