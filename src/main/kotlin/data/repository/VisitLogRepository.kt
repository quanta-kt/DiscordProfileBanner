package data.repository

import data.models.Stat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

class VisitLogRepository(private val ds: DataSource) {

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
    }

    suspend fun logVisit(ip: String, countryCode: String?, instant: Instant, userId: Long) = withContext(Dispatchers.IO) {
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

    suspend fun getUserVisitStats(userId: Long) : Stat = withContext(Dispatchers.IO) {
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
                        it.getString("country") to it.getLong("visit_count")
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

    suspend fun deleteAll(): Unit = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_DELETE_ALL).use {
                it.execute()
            }
        }
    }
}