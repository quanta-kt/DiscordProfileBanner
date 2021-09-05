package routes

import data.tables.Visit
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.rest.util.Image
import generateImage
import guildId
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.ktor.ext.inject
import utils.getOrNull
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.imageio.ImageIO

fun Route.horizontalBanner() {

    val bot: GatewayDiscordClient by inject()
    val database: Database by inject()

    get("{id}.png") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val guild =
            bot.getGuildById(guildId).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        val member = guild.getMemberById(Snowflake.of(id)).awaitSingleOrNull() ?: return@get call.respond(
            HttpStatusCode.NotFound
        )

        // Log this request
        launch {
            newSuspendedTransaction(Dispatchers.IO, database) {
                Visit.new {
                    ip = call.request.origin.remoteHost
                    userId = id
                    timestamp = Instant.now()
                }
            }
        }

        val presence = member.presence.awaitSingleOrNull()
        val activity = presence?.activities
            ?.firstOrNull { it.type != Activity.Type.CUSTOM }

        val image = generateImage(
            member.username,
            member.discriminator.toInt(),
            member.getAvatarUrl(Image.Format.PNG).getOrNull() ?: member.defaultAvatarUrl,
            presence?.status,
            activity
        )

        val bytes = async(Dispatchers.IO) {
            val os = ByteArrayOutputStream()
            ImageIO.write(image, "png", os)
            os.toByteArray()
        }

        call.respondBytes(bytes.await(), ContentType.Image.PNG)
    }
}

fun Routing.bannerRoutes() {
    route("banner") {
        horizontalBanner()
    }
}
