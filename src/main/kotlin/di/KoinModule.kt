package di

import bot.createBot
import data.createDatabaseConnection
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module

val appModule = module {
    single(createdAtStart = true) {
        createBot()
    }

    single(createdAtStart = true) {
        createDatabaseConnection()
    }

    single {
        HttpClient(CIO)
    }
}