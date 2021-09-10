package routes

import data.models.LeaderboardRank
import data.repository.VisitLogRepository
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

private fun Route.home() {

    val visitLogRepository: VisitLogRepository by inject()

    get {
        val leaderboardRanks: List<LeaderboardRank> = visitLogRepository.getLeaderboardData()
        call.respond(FreeMarkerContent(
            "index.ftl",
            mapOf("ranks" to leaderboardRanks)
        ))
    }
}

private fun Route.stats() {
    get("/stats/{id}") {
        val userId = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
    }
}

fun Routing.siteRoutes() {
    route("") {
        home()
        stats()
    }
}