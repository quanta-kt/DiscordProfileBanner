package routes

import data.models.LeaderboardRank
import data.repository.VisitLogRepository
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.rest.http.client.ClientException
import io.ktor.application.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
    val visitLogRepository: VisitLogRepository by inject()
    val bot: GatewayDiscordClient by inject()

    get("/stats/{id}") {
        val userId = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

        val user = try {
            bot.getUserById(Snowflake.of(userId)).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        } catch (e: ClientException) {
            return@get call.respond(HttpStatusCode.NotFound)
        }

        val stats = visitLogRepository.getUserVisitStats(userId)
        call.respond(FreeMarkerContent(
            "user-stats.ftl",
            mapOf(
               "stat" to stats,
                "username" to user.tag,
                "profilePicture" to user.avatarUrl
            )
        ))
    }
}

fun Routing.siteRoutes() {
    route("") {
        home()
        stats()
    }
}