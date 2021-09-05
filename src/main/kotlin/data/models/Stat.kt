package data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stat(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("total_visits")
    val totalVisits: Long,
    @SerialName("unique_visits")
    val uniqueVisits: Long,
    @SerialName("top_countries")
    val topCounties: Map<String, Long>
)