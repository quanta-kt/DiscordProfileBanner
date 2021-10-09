package data.models

import kotlinx.serialization.Serializable

@Serializable
data class ResolvedUserId(
    val id: String
)