package bot.commands

import data.repository.BannerPreferenceRepository
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import kotlinx.coroutines.reactor.awaitSingle
import org.koin.mp.KoinPlatformTools
import utils.getOrNull

/**
 * Command to change frame color on the banner.
 */
object FrameColor : Command {
    override val name: String = "framecolor"

    override fun commandRequest(): ApplicationCommandRequest = ApplicationCommandRequest.builder()
        .name(name)
        .description("Set your avatar's frame color")
        .addOption(ApplicationCommandOptionData.builder()
            .name("color")
            .description("New color of your avatar frame, leave blank to use default")
            .type(ApplicationCommandOptionType.STRING.value)
            .required(false)
            .build())
        .build()

    private val repository: BannerPreferenceRepository by lazy { KoinPlatformTools.defaultContext().get().get() }

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val colorCode =  try {
            event.getOption("color")
                .getOrNull()
                ?.value
                ?.getOrNull()
                ?.asString()
                ?.removePrefix("#")
                ?.toInt(16)
        } catch (e: NumberFormatException) {
            event.replyEphemeral("Invalid color code").awaitSingle()
            return
        }

        repository.setFrameColor(userId, colorCode)

        if (colorCode == null) {
            event.replyEphemeral("Avatar frame color was reset.").awaitSingle()
        } else {
            event.replyEphemeral("Avatar frame color updated.").awaitSingle()
        }
    }
}