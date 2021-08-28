import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.retriever.EntityRetrievalStrategy
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.util.Image
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import utils.getOrNull
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

val guildId: Snowflake = Snowflake.of(System.getenv("GUILD_ID"))
val token = System.getenv("TOKEN") ?: error("TOKEN environment variable is not set.")
val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

fun createBot() : GatewayDiscordClient {
    return DiscordClient.create(token)
        .gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
        .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST)
        .login()
        .block() ?: error("GatewayDiscordClient.login().block() returned null.")
}

fun main() {

    val bot = createBot()

    embeddedServer(Netty, port = port) {
        routing {
            get("/{id}.png") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val guild = bot.getGuildById(guildId).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
                val member = guild.getMemberById(Snowflake.of(id)).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

                val presence = member.presence.awaitSingle()

                val image = generateImage(
                    member.username,
                    member.discriminator.toInt(),
                    member.getAvatarUrl(Image.Format.PNG).getOrNull() ?: member.defaultAvatarUrl,
                    presence.status,
                    presence.activity.getOrNull()
                )

                val bytes = async(Dispatchers.IO) {
                    val os = ByteArrayOutputStream()
                    ImageIO.write(image, "png", os)
                    os.toByteArray()
                }

                call.respondBytes(bytes.await(), ContentType.Image.PNG)
            }
        }
    }.start(true)
}
