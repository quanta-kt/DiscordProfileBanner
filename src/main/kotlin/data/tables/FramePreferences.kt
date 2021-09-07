package data.tables

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

/**
 * User's preferences for avatar frames
 */
object FramePreferences : IdTable<Long>("frame_preference") {
    val userId = long("user_id")
    val color = integer("color").nullable()
    val enabled = bool("enabled")

    override val id = userId.entityId()
    override val primaryKey: PrimaryKey = PrimaryKey(userId)
}

class FramePreference(id: EntityID<Long>) : LongEntity(id) {
    companion object: LongEntityClass<FramePreference>(FramePreferences)
    val userId by FramePreferences.userId
    val color by FramePreferences.color
    val enabled by FramePreferences.enabled
}
