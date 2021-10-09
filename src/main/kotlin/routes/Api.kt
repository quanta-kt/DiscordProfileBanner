package routes

import Constants
import data.models.ResolvedUserId
import data.repository.VisitLogRepository
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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

fun Route.resolveUserId() {
    val bot: GatewayDiscordClient by inject()

    get("resolve-user/{username}") {
        val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val user = bot.getGuildById(Constants.guildId).awaitSingle().members.filter {
            it.tag == username
        }.awaitFirstOrNull()

        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(ResolvedUserId(user.id.asString()))
        }
    }
}

fun Routing.apiRoutes() {
    route("api") {
        userStats()
        resolveUserId()
    }
}