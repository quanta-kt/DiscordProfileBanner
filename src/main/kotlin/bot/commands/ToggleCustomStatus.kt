package bot.commands

import data.repository.BannerPreferenceRepository
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.koin.mp.KoinPlatformTools

/**
 * Command to toggle visibility of custom status in the banner.
 */
object ToggleCustomStatus : Command {
    override val name: String = "togglecustomstatus"

    override fun commandRequest(): ApplicationCommandRequest =
        ApplicationCommandRequest.builder()
            .name(name)
            .description("Toggle visibility of your custom status on your banner")
            .build()

    private val repository: BannerPreferenceRepository by lazy { KoinPlatformTools.defaultContext().get().get() }

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val state = repository.toggleCustomStatusVisibility(userId)

        if (state) {
            event.replyEphemeral("Custom status will be shown on your banner.").awaitSingle()
        } else {
            event.replyEphemeral("Custom status will be hidden from your banner").awaitSingle()
        }
    }
}