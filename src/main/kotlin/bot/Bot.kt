package bot

import Constants
import bot.commands.commands
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.SlashCommandEvent
import discord4j.core.retriever.EntityRetrievalStrategy
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import kotlin.concurrent.thread

fun createBot(): GatewayDiscordClient {
    val restClient = DiscordClient.create(Constants.botToken)
    val gatewayClient = restClient.gateway()
        .setEnabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
        .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST)
        .login()
        .block() ?: error("GatewayDiscordClient.login().block() returned null.")

    val applicationId = restClient.applicationId.block() ?: error("Unable to get application ID")

    commands.values.forEach {
        restClient.applicationService.createGuildApplicationCommand(
            applicationId,
            Constants.guildId.asLong(),
            it.commandRequest()
        ).block()
    }

    gatewayClient.on(object : ReactiveEventAdapter() {
        override fun onSlashCommand(event: SlashCommandEvent): Publisher<*> {
            return mono {
                commands[event.commandName]?.execute(event)
            }
        }
    }).subscribe()

    Runtime.getRuntime().addShutdownHook(thread(false) {
        gatewayClient.logout().block()
    })

    return gatewayClient
}