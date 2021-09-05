package api

import data.models.Stat
import data.tables.Visit
import data.tables.Visits
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import module
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class UserStatsTest : KoinTest {

    private val database: Database by inject()

    private fun deleteVisits() {
        transaction(database) {
            Visits.deleteAll()
        }
    }

    @Test
    fun testGetUserStats() {
        val userId = 123456789L

        withTestApplication({ module(testing = true) }) {

            deleteVisits()

            // Create mock visit records
            transaction(database) {
                for (i in 1..100) {
                    println(i)
                    Visit.new {
                        ip = "${i % 15}.${i % 10}.${i % 5}.${i % 2}"
                        country = when (i) {
                            1 -> "GB"
                            2 -> "BR"
                            3 -> "MX"
                            in 4..10 -> "US"
                            in 11..15 -> "IN"
                            in 16..20 -> "KR"
                            in 21..29 -> "RU" // 9
                            in 30..40 -> "FR" // 11
                            in 41..59 -> "NP" // 19
                            in 60..79 -> "VN" // 20
                            in 80..100 -> "CN" // 21
                            else -> null
                        }
                        timestamp = Instant.now()
                        this.userId = userId
                    }
                }
            }

            val response = handleRequest(HttpMethod.Get, "/api/stats/$userId").response
            assertNotNull(response.content)
            val stat: Stat = Json.decodeFromString(response.content!!)

            assertEquals(100, stat.totalVisits)
            assertEquals(30, stat.uniqueVisits)
            val topCountries = stat.topCounties.entries
                .sortedByDescending { it.value }
                .map { it.key }
            assertEquals(listOf("CN", "VN", "NP", "FR", "RU"), topCountries)
            assertEquals(21, stat.topCounties["CN"])
            assertEquals(20, stat.topCounties["VN"])
            assertEquals(19, stat.topCounties["NP"])
            assertEquals(11, stat.topCounties["FR"])
            assertEquals(9, stat.topCounties["RU"])
        }
    }
}