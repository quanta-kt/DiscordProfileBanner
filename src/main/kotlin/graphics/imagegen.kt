import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Status
import exceptions.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.imgscalr.Scalr
import org.slf4j.LoggerFactory
import utils.ResourceHelper
import java.awt.*
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO


const val IMAGE_WIDTH = 700
const val IMAGE_HEIGHT = 160
const val MARGIN = 10
const val AVATAR_SIZE = IMAGE_HEIGHT - (MARGIN * 2)
const val USERNAME_TEXT_SIZE = IMAGE_HEIGHT / 4
const val TEXT_SIZE = IMAGE_HEIGHT / 10

const val DOT_BORDER_SIZE = 8
const val DOT_SIZE = (AVATAR_SIZE.toFloat() / 3).toInt()
const val INNER_DOT_SIZE = DOT_SIZE - (DOT_BORDER_SIZE * 2)

private val logger = LoggerFactory.getLogger("ImageGen")

/**
 * Color for online dot
 */
val colorOnline = Color(59, 165, 93)

/**
 * Color for offline ring
 */
val colorOffline = Color(116, 127, 141)

/**
 * Color for idle moon
 */
val colorIdle = Color(250, 168, 26)

/**
 * Color for DnD
 */
val colorDnd = Color(237, 66, 69)

/**
 * Background color
 */
val colorBackground = Color(54, 57, 63)

/**
 * Discord logo
 */
val discordLogo: BufferedImage = ImageIO.read(ResourceHelper.getResource("discord_logo.png")).let {
    val img = Scalr.resize(it, Scalr.Mode.FIT_TO_HEIGHT, IMAGE_HEIGHT / 4)
    it.flush()
    img
}

/**
 * Font for drawing text
 */
val fontRegular: Font =
    Font.createFont(
        Font.TRUETYPE_FONT,
        ResourceHelper.getResource("Poppins-Regular.ttf")?.openStream()
            ?: error("Unable to load Poppins Regular font.")
    ).deriveFont(TEXT_SIZE.toFloat())

/**
 * Large and bold font for drawing username
 */
val fontUsername: Font =
    Font.createFont(
        Font.TRUETYPE_FONT,
        ResourceHelper.getResource("Poppins-Bold.ttf")?.openStream()
            ?: error("Unable to load Poppins Bold font.")
    ).deriveFont(USERNAME_TEXT_SIZE.toFloat())

/**
 * large regular font for drawing tag
 */
val fontTag: Font =
    Font.createFont(
        Font.TRUETYPE_FONT,
        ResourceHelper.getResource("Poppins-Regular.ttf")?.openStream()
            ?: error("Unable to load Poppins Regular font.")
    ).deriveFont(USERNAME_TEXT_SIZE.toFloat())

suspend fun generateImage(
    username: String,
    tag: Int,
    avatarUrl: String,
    status: Status,
    activity: Activity?
): BufferedImage = withContext(Dispatchers.Default) {
    // Download avatar
    val avatar = async(Dispatchers.IO) {
        val connection = URL(avatarUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val responseStatus = connection.responseCode
        if (responseStatus in 200..300) {
            return@async ImageIO.read(connection.inputStream)
        } else {
            logger.error("Error while reading avatar.\n" +
                    "Response: ${connection.inputStream.readNBytes(2147483647)}")
            throw HttpException(connection.responseMessage)
        }
    }

    val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR)

    with(image.createGraphics()) {
        applyQualityRenderingHints()

        // Draw avatar
        val avatarScaled = Scalr.resize(avatar.await(), AVATAR_SIZE)
        avatar.await().flush()
        drawMaskedAvatar(
            create(
                MARGIN,
                MARGIN,
                AVATAR_SIZE,
                AVATAR_SIZE
            ) as Graphics2D,
            avatarScaled
        )

        // Draw status dot
        val dotGraphics = create(
            MARGIN + AVATAR_SIZE - DOT_SIZE,
            MARGIN + AVATAR_SIZE - DOT_SIZE,
            DOT_SIZE,
            DOT_SIZE
        ) as Graphics2D

        when (status) {
            Status.ONLINE -> drawOnlineDot(dotGraphics)
            Status.OFFLINE -> drawOfflineRing(dotGraphics)
            Status.IDLE -> drawIdleMoon(dotGraphics)
            Status.DO_NOT_DISTURB -> drawDoNotDisturbDot(dotGraphics)
            Status.UNKNOWN -> drawOfflineRing(dotGraphics)
            Status.INVISIBLE -> drawOfflineRing(dotGraphics)
        }

        // Translate to make sure we don't interfere with the avatar
        translate(MARGIN * 4 + AVATAR_SIZE, 0)

        // Draw username and tag
        font = fontUsername
        color = Color.WHITE
        val usernameWidth = fontMetrics.stringWidth(username)
        drawString(username, 0, USERNAME_TEXT_SIZE + MARGIN)

        font = fontTag
        color = Color(0xB9BBBE)
        drawString("#$tag", usernameWidth, USERNAME_TEXT_SIZE + MARGIN)

        // Draw status
        color = Color.WHITE
        font = fontRegular
        drawString(
            "Status: ${status.toString().replace("_", " ").toLowerCase().capitalize()}",
            0,
            TEXT_SIZE + USERNAME_TEXT_SIZE + (MARGIN * 4)
        )

        // Draw activity
        if (activity != null) {
            drawString(activity.format(), 0, (TEXT_SIZE * 2) + USERNAME_TEXT_SIZE + (MARGIN * 5))

            // Draw activity state
            activity.state.ifPresent {
                drawString(it, 0, (TEXT_SIZE * 3) + USERNAME_TEXT_SIZE + (MARGIN * 6))
            }
        }

        // Draw discord logo
        translate(-(MARGIN * 4 + AVATAR_SIZE), 0)
        drawImage(discordLogo, IMAGE_WIDTH - discordLogo.width - MARGIN, MARGIN, null)

        // Fill background
        composite = AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1f)
        color = colorBackground
        fillRoundRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, MARGIN, MARGIN)

        dispose()
    }

    image
}

fun Activity.format(): String {
    val label = when (type) {
        Activity.Type.PLAYING -> "Playing"
        Activity.Type.STREAMING -> "Streaming"
        Activity.Type.LISTENING -> "Listening"
        Activity.Type.WATCHING -> "Watching"
        Activity.Type.COMPETING -> "Competing"
        Activity.Type.CUSTOM -> "Status"
        Activity.Type.UNKNOWN -> "Playing"
        else -> "Playing"
    }

    return "$label: $name"
}

fun drawMaskedAvatar(graphics: Graphics2D, avatar: BufferedImage) {
    // Create a mask
    val mask = BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
    val maskGraphics = mask.createGraphics()
    maskGraphics.applyQualityRenderingHints()
    maskGraphics.fillOval(0, 0, AVATAR_SIZE, AVATAR_SIZE)
    maskGraphics.dispose()

    // Apply masked
    graphics.applyQualityRenderingHints()
    graphics.drawImage(avatar, 0, 0, null)
    graphics.composite = AlphaComposite.getInstance(AlphaComposite.DST_IN)
    graphics.drawImage(mask, 0, 0, null)
}

fun Graphics2D.applyQualityRenderingHints() {
    setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
    setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
    setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
}


fun drawOnlineDot(graphics: Graphics2D) {
    // Draw outer dot for border
    drawOuterDot(graphics)

    graphics.color = colorOnline
    graphics.fillOval(
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        INNER_DOT_SIZE,
        INNER_DOT_SIZE
    )
}

fun drawOfflineRing(graphics: Graphics2D) {
    drawOuterDot(graphics)

    graphics.color = colorOffline
    graphics.fillOval(
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        INNER_DOT_SIZE,
        INNER_DOT_SIZE
    )

    val rule = AlphaComposite.CLEAR
    val comp: Composite = AlphaComposite.getInstance(rule, 0f)
    graphics.composite = comp
    graphics.stroke = BasicStroke(5f)
    graphics.fillOval(
        DOT_BORDER_SIZE * 2,
        DOT_BORDER_SIZE * 2,
        INNER_DOT_SIZE - DOT_BORDER_SIZE * 2,
        INNER_DOT_SIZE - DOT_BORDER_SIZE * 2,
    )
}

fun drawIdleMoon(graphics: Graphics2D) {
    drawOuterDot(graphics)

    graphics.color = colorIdle
    graphics.fillOval(
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        INNER_DOT_SIZE,
        INNER_DOT_SIZE
    )

    // Erase out a small circle to make it look like a partial moon
    graphics.composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f)
    graphics.fillOval(
        DOT_BORDER_SIZE / 2,
        DOT_BORDER_SIZE / 2,
        DOT_SIZE / 2,
        DOT_SIZE / 2,
    )
}

fun drawDoNotDisturbDot(graphics: Graphics2D) {
    // Draw outer dot for border
    drawOuterDot(graphics)

    graphics.color = colorDnd
    graphics.fillOval(
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        INNER_DOT_SIZE,
        INNER_DOT_SIZE
    )

    // cut out a round rect
    val rule = AlphaComposite.CLEAR
    val comp: Composite = AlphaComposite.getInstance(rule, 0f)
    graphics.composite = comp
    graphics.fillRoundRect(
        (DOT_BORDER_SIZE * 1.5).toInt(),
        (DOT_BORDER_SIZE * 2.4).toInt(),
        INNER_DOT_SIZE - DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE,
        DOT_BORDER_SIZE
    )
}

fun drawOuterDot(graphics: Graphics2D) {
    val rule = AlphaComposite.CLEAR
    val comp: Composite = AlphaComposite.getInstance(rule, 0f)
    graphics.composite = comp
    graphics.stroke = BasicStroke(5f)
    graphics.fillOval(
        0,
        0,
        DOT_SIZE,
        DOT_SIZE
    )

    // Restore composite
    graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
}