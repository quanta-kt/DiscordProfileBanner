package bot.commands

import data.tables.FramePreferences
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import utils.getOrNull

object EditFrame : Command {
    override val name: String = "avatarframe"

    override fun commandRequest(): ApplicationCommandRequest = ApplicationCommandRequest.builder()
        .name(name)
        .description("Set your avatar frame preferences")
        .addOption(ApplicationCommandOptionData.builder()
            .name("enabled")
            .description("Should we show a frame on your banner?")
            .type(ApplicationCommandOptionType.BOOLEAN.value)
            .required(false)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("color")
            .description("New color of your avatar frame")
            .type(ApplicationCommandOptionType.STRING.value)
            .required(false)
            .build())
        .build()

    override suspend fun execute(event: SlashCommandEvent) {
        val userId = event.interaction.user.id.asLong()
        val colorOption = event.getOption("color").getOrNull()
        val colorInput = colorOption?.value?.getOrNull()?.asString()
        val enabled = event.getOption("enabled").getOrNull()?.value?.getOrNull()?.asBoolean()

        val colorCode = try {
            colorInput
                ?.removePrefix("#") // Get rid of "#" if it begins with one
                ?.toInt(16)
        } catch (e: NumberFormatException) {
            event.replyEphemeral("Invalid color hex").awaitSingle()
            return
        }

        newSuspendedTransaction(Dispatchers.IO) {
            FramePreferences.insertIgnore {
                it[this.userId] = userId
                it[this.color] = colorCode
                it[this.enabled] = enabled ?: true
            }
            FramePreferences.update({ FramePreferences.userId eq userId }) {
                if (colorCode != null)
                    it[this.color] = colorCode
                if (enabled != null)
                    it[this.enabled] = enabled

                // Reset to defaults if both arguments are null
                if (colorCode == null && enabled == null) {
                    it[this.color] = null
                    it[this.enabled] = true
                }
            }
        }

        if (colorCode == null && enabled == null) {
            event.replyEphemeral("Avatar frame was reset.").awaitSingle()
        } else {
            event.replyEphemeral("Avatar frame updated.").awaitSingle()
        }
    }
}