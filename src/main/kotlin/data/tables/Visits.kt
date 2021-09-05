package data.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Visits : IntIdTable("visit") {
    val ip = text("ip")
    val timestamp = timestamp("timestamp")
    val userId = long("user_id")
    val country = text("country").nullable()
}

class Visit(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<Visit>(Visits)
    var ip by Visits.ip
    var timestamp by Visits.timestamp
    var userId by Visits.userId
    var country by Visits.country
}