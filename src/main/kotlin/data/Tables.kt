package data

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Visits : IntIdTable("visit") {
    val ip = text("ip")
    val timestamp = timestamp("timestamp")
    val userId = long("user_id")
}