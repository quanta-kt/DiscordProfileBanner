package data.repository

import data.models.LeaderboardRank
import data.models.Stat
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.rest.http.client.ClientException
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

class VisitLogRepository(private val ds: DataSource, private val bot: GatewayDiscordClient) {

    companion object {
        private const val QUERY_LOG_VISIT = """
            INSERT INTO visit(
                ip, country, "timestamp", user_id
            )
            VALUES(?, ?, ?, ?);
        """

        private const val QUERY_GET_USER_VISITS = """
            SELECT
                COUNT(*) as total_count,
                COUNT(distinct ip) as unique_count
            FROM visit
            WHERE user_id = ?;
        """

        private const val QUERY_GET_USER_TOP_COUNTRIES = """
            SELECT
                country,
                COUNT(distinct ip) AS visit_count
            FROM visit
            WHERE user_id = ?
            GROUP BY country
            ORDER BY visit_count DESC
            LIMIT 5;
        """

        private const val QUERY_DELETE_ALL = "DELETE FROM visit;"

        private const val QUERY_GET_LEADERBOARD = """
            SELECT
                user_id,
                COUNT(ip) AS total_visits,
                COUNT(DISTINCT ip) AS unique_visits
            FROM visit
            GROUP BY user_id
            ORDER BY unique_visits DESC, total_visits DESC
            LIMIT 10;
        """
    }

    suspend fun logVisit(ip: String, countryCode: String?, instant: Instant, userId: Long) =
        withContext(Dispatchers.IO) {
            ds.connection.use { connection ->
                connection.prepareStatement(QUERY_LOG_VISIT).use { statement ->
                    statement.setString(1, ip)
                    statement.setStringOrNull(2, countryCode)
                    statement.setTimestamp(3, Timestamp.from(instant))
                    statement.setLong(4, userId)
                    statement.execute()
                }
            }
        }

    suspend fun getUserVisitStats(userId: Long): Stat = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->

            val totalVisits: Long
            val uniqueVisits: Long
            connection.prepareStatement(QUERY_GET_USER_VISITS).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    totalVisits = resultSet.getLong("total_count")
                    uniqueVisits = resultSet.getLong("unique_count")
                }
            }

            val topCountries = connection.prepareStatement(QUERY_GET_USER_TOP_COUNTRIES).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { resultSet ->
                    resultSet.iterator {
                        (it.getStringOrNull("country") ?: "unknown") to it.getLong("visit_count")
                    }.asSequence().toMap()
                }
            }

            Stat(
                userId = userId,
                totalVisits = totalVisits,
                uniqueVisits = uniqueVisits,
                topCountries = topCountries
            )
        }
    }

    suspend fun getLeaderboardData(): List<LeaderboardRank> = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_GET_LEADERBOARD).use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.toList {
                        val id = it.getLong("user_id")
                        val username = try {
                            val user = bot.getUserById(Snowflake.of(id)).awaitSingleOrNull()
                            user?.tag ?: id.toString()
                        } catch (e: ClientException) {
                            id.toString()
                        }
                        LeaderboardRank(
                            username,
                            id,
                            it.getLong("total_visits"),
                            it.getLong("unique_visits"),
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteAll(): Unit = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_DELETE_ALL).use {
                it.execute()
            }
        }
    }
}