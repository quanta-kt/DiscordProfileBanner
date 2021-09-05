package routes

import data.models.Stat
import data.tables.Visits
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.ktor.ext.inject


/**
 * Responds with user's banner visit stat, including total visits, unique visits
 * and count of unique visits from top 5 countries
 */
fun Route.userStats() {

    val database: Database by inject()

    get("stats/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)

        val stat = newSuspendedTransaction(Dispatchers.IO, database) {
            // Query the top 5 countries user got visits from
            val visitsByCountries = Count(Visits.ip, true).alias("visit_count")
            val topCountries = Visits
                .slice(visitsByCountries, Visits.country)
                .select { (Visits.userId eq id) and (Visits.country neq null) }
                .groupBy(Visits.country)
                .orderBy(visitsByCountries, SortOrder.DESC)
                .limit(5)
                .mapNotNull {
                    val country = it[Visits.country] ?: return@mapNotNull null
                    country to it[visitsByCountries]
                }
                .toMap()

            // Query total and unique visits
            val totalCount = Count(Visits.id).alias("total_visits")
            val uniqueCount = Count(Visits.ip, true).alias("unique_visits")
            val totalUniquePair = Visits.slice(totalCount, uniqueCount)
                .select { Visits.userId eq id }
                .first()
                .let {
                    Pair(
                        it[totalCount],
                        it[uniqueCount]
                    )
                }

            Stat(
                id,
                totalUniquePair.first,
                totalUniquePair.second,
                topCountries
            )
        }

        call.respond(stat)
    }
}

fun Routing.apiRoutes() {
    route("api") {
        userStats()
    }
}