package bot.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandRequest

interface Command {
    val name: String
    fun commandRequest(): ApplicationCommandRequest
    suspend fun execute(event: SlashCommandEvent)
}

val commands : Map<String, Command> = mapOf(
    EditFrame.name to EditFrame
)