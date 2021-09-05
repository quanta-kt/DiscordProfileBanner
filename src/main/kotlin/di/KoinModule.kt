package di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import discord4j.core.DiscordClient
import discord4j.core.retriever.EntityRetrievalStrategy
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import kotlin.concurrent.thread

private fun createDatabaseConnection() : Database {
    val databaseUrl = System.getenv("JDBC_DATABASE_URL") ?: error("DATABASE_URL environment variable is not set.")
    val config = HikariConfig().apply {
        jdbcUrl = databaseUrl
    }
    val ds = HikariDataSource(config)
    val db =  Database.connect(ds)

    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        ds.close()
    })

    // Migrate database
    Flyway.configure().dataSource(ds).load().apply {
        baseline()
        migrate()
    }

    return db
}

val appModule = module {
    single(createdAtStart = true) {
        val token = System.getenv("TOKEN") ?: error("TOKEN environment variable is not set.")
        DiscordClient.create(token)
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
            .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST)
            .login()
            .block() ?: error("GatewayDiscordClient.login().block() returned null.")
    }

    single(createdAtStart = true) {
        createDatabaseConnection()
    }

    single {
        HttpClient(CIO)
    }
}