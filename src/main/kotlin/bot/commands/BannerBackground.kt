package bot.commands

import data.repository.BannerPreferenceRepository
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.reactor.awaitSingle
import org.koin.mp.KoinPlatformTools
import utils.getOrNull
import java.nio.channels.UnresolvedAddressException


/**
 * Command to update banner background image
 */
object BannerBackground : Command {
    override val name: String = "background"

    override fun commandRequest(): ApplicationCommandRequest =
        ApplicationCommandRequest.builder()
            .name(name)
            .description("Set a background image for your banner.")
            .addOption(ApplicationCommandOptionData.builder()
                .name("imageurl")
                .description("URL of the image to use, leave empty to use default")
                .type(ApplicationCommandOptionType.STRING.value)
                .required(false)
                .build())
            .build()

    private val httpClient: HttpClient by lazy { KoinPlatformTools.defaultContext().get().get() }
    private val repository: BannerPreferenceRepository by lazy { KoinPlatformTools.defaultContext().get().get() }

    private val allowedImageTypes = listOf(
        ContentType.Image.JPEG,
        ContentType.Image.PNG,
    )

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val imageUrl = event.getOption("imageurl").getOrNull()?.value?.getOrNull()?.asString()

        // Validate URL and url contents
        if (imageUrl != null) {
            try {
                val response: HttpResponse = httpClient.get(imageUrl)
                if (response.contentType() !in allowedImageTypes) {
                    event.replyEphemeral("Not a valid URL of an image.").awaitSingle()
                    return
                }
            } catch (e: URLParserException) {
                event.replyEphemeral("Not a valid URL.").awaitSingle()
                return
            } catch (e: UnresolvedAddressException) {
                event.replyEphemeral("Failed to resolve address").awaitSingle()
                return
            }
        }

        repository.setBackgroundImageUrl(userId, imageUrl)

        if (imageUrl != null) {
            event.replyEphemeral("Background image updated.").awaitSingle()
        } else {
            event.replyEphemeral("Background was reset to default.").awaitSingle()
        }
    }
}