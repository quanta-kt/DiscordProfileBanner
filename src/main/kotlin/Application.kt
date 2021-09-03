import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import data.Visits
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.retriever.EntityRetrievalStrategy
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.util.Image
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import utils.getOrNull
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.concurrent.thread


val guildId: Snowflake = Snowflake.of(System.getenv("GUILD_ID"))
val token = System.getenv("TOKEN") ?: error("TOKEN environment variable is not set.")
val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

private fun createBot() : GatewayDiscordClient {
    return DiscordClient.create(token)
        .gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
        .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST)
        .login()
        .block() ?: error("GatewayDiscordClient.login().block() returned null.")
}

private fun createDatabaseConnection() : Database {
    val databaseUrl = System.getenv("JDBC_DATABASE_URL") ?: error("DATABASE_URL environment variable is not set.")
    val config = HikariConfig().apply {
        jdbcUrl = databaseUrl
    }
    val ds = HikariDataSource(config)
    val db =  Database.connect(ds)

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        ds.close()
    })

    transaction {
        SchemaUtils.create(Visits)
    }

    return db
}

private suspend fun logVisit(call: ApplicationCall, requestedUserId: Long) {
    newSuspendedTransaction(Dispatchers.IO) {
        Visits.insert {
            it[ip] = call.request.origin.remoteHost
            it[userId] = requestedUserId
            it[timestamp] = Instant.now()
        }
    }
}

fun main() {

    val bot = createBot()
    createDatabaseConnection()

    embeddedServer(Netty, port = port) {
        install(XForwardedHeaderSupport)

        routing {
            get("/{id}.png") {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val guild = bot.getGuildById(guildId).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
                val member = guild.getMemberById(Snowflake.of(id)).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

                // Log this request
                launch { logVisit(call, id) }

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
