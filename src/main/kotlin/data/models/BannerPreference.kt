package data.models

data class BannerPreference(
    val userId: Long,
    val frameColor: Int?,
    val backgroundImageUrl: String?,
    val frameVisible: Boolean,
    val tagVisible: Boolean,
    val customStatusVisible: Boolean,
)
