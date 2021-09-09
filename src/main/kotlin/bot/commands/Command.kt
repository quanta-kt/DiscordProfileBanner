package bot.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest

interface Command {
    val name: String
    fun commandRequest(): ApplicationCommandRequest
    suspend fun execute(event: SlashCommandEvent)
}

/**
 * A list of all available commands
 */
val commands : Map<String, Command> = mapOf(
    FrameColor.name to FrameColor,
    ToggleFrame.name to ToggleFrame,
    ToggleTag.name to ToggleTag,
    ToggleCustomStatus.name to ToggleCustomStatus,
    BannerBackground.name to BannerBackground,
)