package data.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardRank(
    @Serializable
    val username: String,
    @Serializable
    val userId: Long,
    @Serializable
    val totalVisits: Long,
    @Serializable
    val uniqueVisits: Long,
)
