package api

import data.models.LeaderboardRank
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
import java.math.BigInteger
import java.net.Inet4Address
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

    @Test
    fun testLeaderboard() {

        // Generates sequential IP addresses
        val ipGenerator = object {
            private var state: BigInteger = BigInteger.valueOf(0xf00000)

            fun next(): String {
                state++
                return get()
            }

            fun get(): String {
                return Inet4Address.getByAddress(state.toByteArray()).hostAddress
            }
        }

        val mockLeaderboard = mapOf(
            885845205201334333 to Pair(49, 20),
            595255377650647141 to Pair(50, 20),
            721012149933310029 to Pair(120, 30),
            402465792324665355 to Pair(150, 50),
            791521836121980961 to Pair(180, 70),
        )

        val expectedRanks = mapOf(
            885845205201334333 to 4,
            595255377650647141 to 3,
            721012149933310029 to 2,
            402465792324665355 to 1,
            791521836121980961 to 0,
        )

        fun assertLeaderboardData(rank: LeaderboardRank, rankIndex: Long) {
            assertEquals(expectedRanks[rank.userId]?.toLong(), rankIndex)
            val expected = mockLeaderboard[rank.userId]
            assertEquals(expected?.first?.toLong(), rank.totalVisits)
            assertEquals(expected?.second?.toLong(), rank.uniqueVisits)
        }


        withTestApplication({ module(testing = true) }) {
            runBlocking {

                visitLogRepository.deleteAll()

                mockLeaderboard.forEach {
                    val userId = it.key
                    val totalVisits = it.value.first
                    val uniqueVisits = it.value.second

                    // Account for unique visits
                    repeat(uniqueVisits) {
                        val ip = ipGenerator.next()
                        visitLogRepository.logVisit(ip, "XX", Instant.now(), userId)
                    }

                    // Account for total visits
                    repeat(totalVisits - uniqueVisits) {
                        val ip = ipGenerator.get()
                        visitLogRepository.logVisit(ip, "XX", Instant.now(), userId)
                    }
                }

                val leaderboard = visitLogRepository.getLeaderboardData()
                leaderboard.forEachIndexed { index, leaderboardRank ->
                    assertLeaderboardData(leaderboardRank, index.toLong())
                }
            }
        }
    }
}