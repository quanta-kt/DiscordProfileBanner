package bot.commands

import data.repository.BannerPreferenceRepository
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.koin.mp.KoinPlatformTools

/**
 * Command to toggle visibility of user's tag on the banner.
 */
object ToggleTag : Command {
    override val name: String = "toggletag"

    override fun commandRequest(): ApplicationCommandRequest =
        ApplicationCommandRequest.builder()
            .name(name)
            .description("Toggle visibility of your Discord tag in the banner")
            .build()

    private val repository: BannerPreferenceRepository by lazy { KoinPlatformTools.defaultContext().get().get() }

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val state = repository.toggleTagVisibility(userId)

        if (state) {
            event.replyEphemeral("Your tag will be shown on the banner.").awaitSingle()
        } else {
            event.replyEphemeral("Your tag will not be shown on the banner.").awaitSingle()
        }
    }
}