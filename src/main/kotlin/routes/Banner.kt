package routes

import Constants
import data.repository.BannerPreferenceRepository
import data.repository.VisitLogRepository
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.Activity
import discord4j.rest.util.Image
import graphics.ImageGenerationRequest
import graphics.generateImage
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.koin.ktor.ext.inject
import utils.getOrNull
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.imageio.ImageIO

fun Route.horizontalBanner() {

    val bot: GatewayDiscordClient by inject()
    val bannerPreferenceRepository: BannerPreferenceRepository by inject()
    val visitLogRepository: VisitLogRepository by inject()
    val httpClient: HttpClient by inject()

    get("{id}.png") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val guild =
            bot.getGuildById(Constants.guildId).awaitSingleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)
        val member = guild.getMemberById(Snowflake.of(id)).awaitSingleOrNull() ?: return@get call.respond(
            HttpStatusCode.NotFound
        )

        val presence = member.presence.awaitSingleOrNull()
        val activity = presence?.activities
            ?.firstOrNull { it.type != Activity.Type.CUSTOM }

        val bannerPreference = bannerPreferenceRepository.getBannerPreferences(id)

        val imageGenerationRequest = ImageGenerationRequest(
            member.username,
            member.discriminator,
            member.getAvatarUrl(Image.Format.PNG).getOrNull() ?: member.defaultAvatarUrl
        )

        imageGenerationRequest.apply {
            status = presence?.status
            this.activity = activity
            customStatus = presence?.activities?.firstOrNull { it.type == Activity.Type.CUSTOM }?.state?.getOrNull()

            if (bannerPreference != null) {
                bannerPreference.frameColor?.let {
                    frameColor = Color(it)
                }

                showFrame = bannerPreference.frameVisible
                showCustomStatus = bannerPreference.customStatusVisible
                showTag = bannerPreference.tagVisible
                backgroundImageUrl = bannerPreference.backgroundImageUrl
            }
        }

        val image = generateImage(imageGenerationRequest, httpClient)

        val bytes = async(Dispatchers.IO) {
            val os = ByteArrayOutputStream()
            ImageIO.write(image, "png", os)
            os.toByteArray()
        }

        call.respondBytes(bytes.await(), ContentType.Image.PNG)

        // Log this request
        launch {
            val requestIp = call.request.origin.remoteHost
            val countryCode = getCountryCodeForIp(httpClient, requestIp)
            visitLogRepository.logVisit(requestIp, countryCode, Instant.now(), id)
        }
    }
}

fun Routing.bannerRoutes() {
    route("banner") {
        horizontalBanner()
    }
}

/**
 * Calls ipapi.co to retrieve country code corresponding to the IP address.
 */
private suspend fun getCountryCodeForIp(httpClient: HttpClient, ip: String): String? {
    val response: HttpResponse = httpClient.get("https://ipapi.co/$ip/country/")

    if (!response.status.isSuccess()) {
        return null
    }

    val countryCode: String = response.receive()

    return if (countryCode == "Undefined") {
        null
    } else {
        countryCode
    }
}