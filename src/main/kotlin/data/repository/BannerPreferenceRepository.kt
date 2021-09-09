package data.repository

import data.models.BannerPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.DataSource

class BannerPreferenceRepository(private val ds: DataSource) {

    companion object {
        private const val QUERY_SELECT_ALL = """
            SELECT
                frame_color,
                background_image_url,
                frame_visible,
                tag_visible,
                custom_status_visible
            FROM banner_preference
            WHERE user_id = ?;
        """

        private const val QUERY_TOGGLE_FRAME_VISIBILITY = """
            INSERT INTO banner_preference(user_id, frame_visible)
            VALUES(?, FALSE)
            ON CONFLICT(user_id) DO UPDATE
                SET frame_visible = NOT banner_preference.frame_visible
            RETURNING frame_visible;
        """

        private const val QUERY_TOGGLE_TAG_VISIBILITY = """
            INSERT INTO banner_preference(user_id, tag_visible)
            VALUES(?, FALSE)
            ON CONFLICT(user_id) DO UPDATE
                SET tag_visible = NOT banner_preference.tag_visible
            RETURNING tag_visible;
        """

        private const val QUERY_TOGGLE_CUSTOM_STATUS_VISIBILITY = """
            INSERT INTO banner_preference(user_id, custom_status_visible)
            VALUES(?, FALSE)
            ON CONFLICT(user_id) DO UPDATE
                SET custom_status_visible = NOT banner_preference.custom_status_visible
            RETURNING custom_status_visible;
        """

        private const val QUERY_SET_FRAME_COLOR = """
            INSERT INTO banner_preference(user_id, frame_color)
            VALUES(?, ?)
            ON CONFLICT(user_id) DO UPDATE
                SET frame_color = ?;
        """

        private const val QUERY_SET_BACKGROUND_IMAGE_URL = """
            INSERT INTO banner_preference(user_id, background_image_url)
            VALUES(?, ?)
            ON CONFLICT(user_id) DO UPDATE
                SET background_image_url = ?;
        """
    }

    suspend fun getBannerPreferences(userId: Long): BannerPreference? = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            val statement = connection.prepareStatement(QUERY_SELECT_ALL)
            statement.setLong(1, userId)
            val resultSet = statement.executeQuery()

            if (resultSet.next()) {
                BannerPreference(
                    userId = userId,
                    frameColor = resultSet.getIntOrNull("frame_color"),
                    backgroundImageUrl = resultSet.getStringOrNull("background_image_url"),
                    tagVisible = statement.resultSet.getBoolean("tag_visible"),
                    customStatusVisible = statement.resultSet.getBoolean("custom_status_visible"),
                    frameVisible = statement.resultSet.getBoolean("frame_visible")
                )
            } else {
                null
            }
        }
    }

    /**
     * Updates color of the frame to be used for frame on the user's banner.
     *
     * @param userId Discord ID of the user
     * @param color Color to use for the frame on user's banner
     */
    suspend fun setFrameColor(userId: Long, color: Int?): Unit = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_SET_FRAME_COLOR).use { statement ->
                statement.setLong(1, userId)
                statement.setIntOrNull(2, color)
                statement.setIntOrNull(3, color)
                statement.execute()
            }
        }
    }

    /**
     * Updates background image url of the user's banner.
     *
     * @param userId Discord ID of the user
     * @param url URL of the background image to use on the banner
     */
    suspend fun setBackgroundImageUrl(userId: Long, url: String?): Unit = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_SET_BACKGROUND_IMAGE_URL).use { statement ->
                statement.setLong(1, userId)
                statement.setString(2, url)
                statement.setString(3, url)
                statement.execute()
            }
        }
    }

    /**
     * Toggles the frame visibility preference for the given user ID and
     * returns the updated state of the same.
     *
     * @param userId Discord ID of of the user
     * @return true if frame was set to be visible, false otherwise
     */
    suspend fun toggleFrameVisibility(userId: Long): Boolean = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_TOGGLE_FRAME_VISIBILITY).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getBoolean(1)
                }
            }
        }
    }

    /**
     * Toggles the Discord tag visibility preference for the given user ID and
     * returns the updated state of the same.
     *
     * @param userId Discord ID of of the user
     * @return true if Discord tag was set to be visible, false otherwise
     */
    suspend fun toggleTagVisibility(userId: Long): Boolean = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_TOGGLE_TAG_VISIBILITY).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getBoolean(1)
                }
            }
        }
    }

    /**
     * Toggles the custom status visibility preference for the given user ID and
     * returns the updated state of the same.
     *
     * @param userId Discord ID of of the user
     * @return true if custom status was set to be visible, false otherwise
     */
    suspend fun toggleCustomStatusVisibility(userId: Long): Boolean = withContext(Dispatchers.IO) {
        ds.connection.use { connection ->
            connection.prepareStatement(QUERY_TOGGLE_CUSTOM_STATUS_VISIBILITY).use { statement ->
                statement.setLong(1, userId)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getBoolean(1)
                }
            }
        }
    }
}