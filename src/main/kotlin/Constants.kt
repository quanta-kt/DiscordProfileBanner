import discord4j.common.util.Snowflake

object Constants {
    val guildId: Snowflake = Snowflake.of(System.getenv("GUILD_ID"))
    val botToken = System.getenv("TOKEN") ?: error("TOKEN environment variable is not set.")
}
