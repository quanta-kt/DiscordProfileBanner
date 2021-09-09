package bot.commands

import data.repository.BannerPreferenceRepository
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.koin.mp.KoinPlatformTools

/**
 * Command to toggle visibility of frame on the banner.
 */
object ToggleFrame : Command {
    override val name: String = "toggleframe"

    override fun commandRequest(): ApplicationCommandRequest =
        ApplicationCommandRequest.builder()
            .name(name)
            .description("Toggle visibility of frame on your banner")
            .build()

    private val repository: BannerPreferenceRepository by lazy { KoinPlatformTools.defaultContext().get().get() }

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val state = repository.toggleFrameVisibility(userId)

        if (state) {
            event.replyEphemeral("Frame will now be shown on your banner.").awaitSingle()
        } else {
            event.replyEphemeral("Frame will be hidden from your banner").awaitSingle()
        }
    }
}