package routes

import data.repository.VisitLogRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject


/**
 * Responds with user's banner visit stat, including total visits, unique visits
 * and count of unique visits from top 5 countries
 */
fun Route.userStats() {

    val visitLogRepository: VisitLogRepository by inject()

    get("stats/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val stat = visitLogRepository.getUserVisitStats(id)
        call.respond(stat)
    }
}

fun Routing.apiRoutes() {
    route("api") {
        userStats()
    }
}