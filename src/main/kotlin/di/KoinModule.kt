package di

import bot.createBot
import data.createHikariDataSource
import data.repository.BannerPreferenceRepository
import data.repository.VisitLogRepository
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.dsl.module

val appModule = module {
    single(createdAtStart = true) {
        createBot()
    }

    single { createHikariDataSource() }
    single { BannerPreferenceRepository(get()) }
    single { VisitLogRepository(get(), get()) }

    single {
        HttpClient(CIO)
    }
}