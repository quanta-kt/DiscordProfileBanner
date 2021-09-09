package api

import data.models.Stat
import data.repository.VisitLogRepository
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import module
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class UserStatsTest : KoinTest {

    private val visitLogRepository: VisitLogRepository by inject()

    @Test
    fun testGetUserStats() {
        val userId = 123456789L

        withTestApplication({ module(testing = true) }) {

            runBlocking {
                visitLogRepository.deleteAll()
            }

            // Create mock visit records
            runBlocking {
                for (i in 1..100) {
                    visitLogRepository.logVisit(
                        ip = "${i % 15}.${i % 10}.${i % 5}.${i % 2}",
                        countryCode = when (i) {
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
                        },
                        instant = Instant.now(),
                        userId = userId
                    )
                }
            }

            val response = handleRequest(HttpMethod.Get, "/api/stats/$userId").response
            assertNotNull(response.content)
            val stat: Stat = Json.decodeFromString(response.content!!)

            assertEquals(100, stat.totalVisits)
            assertEquals(30, stat.uniqueVisits)
            val topCountries = stat.topCountries.entries
                .sortedByDescending { it.value }
                .map { it.key }
            assertEquals(listOf("CN", "VN", "NP", "FR", "RU"), topCountries)
            assertEquals(21, stat.topCountries["CN"])
            assertEquals(20, stat.topCountries["VN"])
            assertEquals(19, stat.topCountries["NP"])
            assertEquals(11, stat.topCountries["FR"])
            assertEquals(9, stat.topCountries["RU"])
        }
    }
}