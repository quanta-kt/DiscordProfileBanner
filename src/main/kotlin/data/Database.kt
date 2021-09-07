package data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import kotlin.concurrent.thread

fun createDatabaseConnection() : Database {
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
        migrate()
    }

    return db
}