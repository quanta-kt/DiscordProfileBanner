package graphics

import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Status
import exceptions.HttpException
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.imgscalr.Scalr
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ResourceHelper
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO


const val IMAGE_WIDTH = 525
const val IMAGE_HEIGHT = 150
const val MARGIN = 15
const val TEXT_MARGIN = 5
const val AVATAR_SIZE = 120
const val USERNAME_TEXT_SIZE = 30
const val TEXT_SIZE = 15

const val STATUS_ICON_SIZE = 24
const val STATUS_ICON_POSITION = 88

private val logger: Logger = LoggerFactory.getLogger("ImageGen")

/**
 * Color for online dot
 */
val colorOnline: Color = Color(59, 165, 93)

/**
 * Color for offline graphics.getRing
 */
val colorOffline: Color = Color(116, 127, 141)

/**
 * Color for idle moon
 */
val colorIdle: Color = Color(250, 168, 26)

/**
 * Color for DnD
 */
val colorDnd: Color = Color(237, 66, 69)

/**
 * Background color
 */
val colorBackground: Color = Color(54, 57, 63)

/**
 * Text color
 */
val textColor: Color = Color.WHITE

/**
 * Lighter text color
 */
val textColorLight: Color = Color(0xB9BBBE)

/**
 * Default color for the avatar frame
 */
val defaultFrameColor: Color = Color(211, 144, 171)

/**
 * Discord logo
 */
val discordLogo: BufferedImage = ImageIO.read(ResourceHelper.getResource("discord_logo.png")).let {
    val img = Scalr.resize(it, Scalr.Mode.FIT_TO_HEIGHT, IMAGE_HEIGHT / 4)
    it.flush()
    img
}

/**
 * Regular sized, regular text
 */
val fontRegular: Font =
    Font.createFont(
        Font.TRUETYPE_FONT,
        ResourceHelper.getResource("Poppins-Regular.ttf")?.openStream()
            ?: error("Unable to load Poppins Regular font.")
    ).deriveFont(TEXT_SIZE.toFloat())

/**
 * Regular sized, bold text
 */
val fontRegularBold: Font = Font.createFont(
    Font.TRUETYPE_FONT,
    ResourceHelper.getResource("Poppins-Bold.ttf")?.openStream()
        ?: error("Unable to load Poppins Bold font.")
).deriveFont(TEXT_SIZE.toFloat())

/**
 * large sized, regular text
 */
val fontLarge: Font = fontRegular.deriveFont(USERNAME_TEXT_SIZE.toFloat())

/**
 * Large sized, bold text
 */
val fontLargeBold: Font = fontRegularBold.deriveFont(USERNAME_TEXT_SIZE.toFloat())

/**
 *  Mask to apply on avatar
 */
val avatarMask: AvatarMask = AvatarMask(AVATAR_SIZE)

/**
 * The frame to draw on the left of avatar
 */
val avatarFrame: AvatarFrame = AvatarFrame(
    IMAGE_HEIGHT,
    (AVATAR_SIZE / 2) + MARGIN,
    MARGIN / 2,
    MARGIN
)

val offlineStatusIcon: OfflineStatusIcon = OfflineStatusIcon(STATUS_ICON_SIZE, colorOffline)

/**
 * Statuses mapped to status indicator shapes to be used.
 */
val statusIcons: Map<Status, Drawable> = mapOf(
    Status.OFFLINE to offlineStatusIcon,
    Status.INVISIBLE to offlineStatusIcon,
    Status.UNKNOWN to offlineStatusIcon,
    Status.ONLINE to OnlineStatusIcon(STATUS_ICON_SIZE, colorOnline),
    Status.IDLE to IdleStatusIcon(STATUS_ICON_SIZE, colorIdle),
    Status.DO_NOT_DISTURB to DndStatusIcon(STATUS_ICON_SIZE, colorDnd)
)

/**
 * Statuses mapped to their colors
 */
val statusColors = mapOf(
    Status.OFFLINE to colorOffline,
    Status.ONLINE to colorOnline,
    Status.IDLE to colorIdle,
    Status.DO_NOT_DISTURB to colorDnd,
    Status.UNKNOWN to colorOffline,
    Status.INVISIBLE to colorOffline,
)

class ImageGenerationRequest(
    val username: String,
    val tag: String,
    val avatarUrl: String,
) {

    var status: Status? = null
    var activity: Activity? = null
    var customStatus: String? = null

    var frameColor: Color = defaultFrameColor
    var showFrame: Boolean = true
    var showTag: Boolean = true
    var showCustomStatus: Boolean = true
    var backgroundImageUrl: String? = null
}

suspend fun generateImage(
    request: ImageGenerationRequest,
    httpClient: HttpClient
): BufferedImage = withContext(Dispatchers.Default) {
    // Download avatar
    val avatar = async {
        val response: HttpResponse = httpClient.get(request.avatarUrl)
        withContext(Dispatchers.IO) {
            ImageIO.read(response.content.toInputStream())
        }
    }

    // Download background image if exists
    val backgroundImage = async {
        request.backgroundImageUrl?.let {
            val response: HttpResponse = httpClient.get(it)
            withContext(Dispatchers.IO) {
                runCatching {
                    ImageIO.read(response.content.toInputStream())
                }.getOrNull()
            }
        }
    }

    val image = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR)

    with(image.createGraphics()) {
        applyQualityRenderingHints()

        drawBackground(this.create() as Graphics2D, backgroundImage.await())

        color = request.frameColor

        if (request.showFrame) {
            draw(avatarFrame)
        }

        translate(MARGIN, MARGIN)

        // Draw avatar
        val avatarScaled = Scalr.resize(avatar.await(), AVATAR_SIZE)
        avatar.await().flush()
        drawMaskedAvatar(
            create(
                0,
                0,
                AVATAR_SIZE,
                AVATAR_SIZE
            ) as Graphics2D,
            avatarScaled
        )

        // Draw status dot
        val dotGraphics = create(
            STATUS_ICON_POSITION,
            STATUS_ICON_POSITION,
            STATUS_ICON_SIZE,
            STATUS_ICON_SIZE
        ) as Graphics2D

        val statusIndicatorDrawable = statusIcons[request.status] ?: offlineStatusIcon
        dotGraphics.draw(statusIndicatorDrawable)

        // Translate to make sure we don't interfere with the avatar
        translate(AVATAR_SIZE + MARGIN, 0)

        // The y co-ordinate to draw text at
        var textY = USERNAME_TEXT_SIZE

        // Draw username
        font = fontLargeBold
        color = textColor
        val usernameWidth = fontMetrics.stringWidth(request.username)
        drawString(request.username, 0, textY)

        // and tag
        if (request.showTag) {
            font = fontLarge
            color = textColorLight
            drawString("#${request.tag}", usernameWidth, textY)
        }

        color = textColor
        font = fontRegularBold

        // Move to next line
        textY += TEXT_SIZE + TEXT_MARGIN
        font = fontRegularBold

        // Draw status
        val statusText = (request.status ?: Status.OFFLINE)
            .toString()
            .replace("_", " ")
            .toLowerCase()
            .capitalize()
        val statusLabel = "Status: "
        val statusLabelWidth = fontMetrics.stringWidth(statusLabel)
        drawString("Status: ", 0, textY)
        color = statusColors[request.status]
        drawString(statusText, statusLabelWidth, textY)

        font = fontRegular
        color = textColor

        // Draw custom status
        if (request.showCustomStatus) {
            request.customStatus?.let {
                // Move to next line
                textY += TEXT_SIZE + TEXT_MARGIN
                drawString(it, 0, textY)
            }
        }

        // Draw activity
        request.activity?.let { activity ->
            // Move to next line
            textY += TEXT_SIZE + TEXT_MARGIN * 2
            font = fontRegular
            drawString(activity.format(), 0, textY)

            // Draw activity state
            activity.state.ifPresent { state ->
                // Move to next line
                textY += TEXT_SIZE + TEXT_MARGIN
                drawString(state, 0, textY)
            }
        }

        // Draw discord logo
        translate(-(MARGIN * 3 + AVATAR_SIZE), 0)
        drawImage(discordLogo, IMAGE_WIDTH - discordLogo.width, 0, null)

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
    maskGraphics.draw(avatarMask)
    maskGraphics.dispose()

    // Create masked image
    val masked = BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB)
    val maskedGraphics = masked.createGraphics()
    maskedGraphics.applyQualityRenderingHints()
    maskedGraphics.drawImage(avatar, 0, 0, null)
    maskedGraphics.composite = AlphaComposite.getInstance(AlphaComposite.DST_IN)
    maskedGraphics.drawImage(mask, 0, 0, null)
    maskGraphics.dispose()

    // Draw masked image
    graphics.drawImage(masked, 0, 0, null)
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
    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
}

fun drawBackground(graphics: Graphics2D, im: BufferedImage?) = with(graphics) {
    if (im == null) {
        composite = AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1f)
        color = colorBackground
        fillRoundRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, MARGIN, MARGIN)
    } else {
        // Create a mask
        val mask = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val maskGraphics = mask.createGraphics()
        maskGraphics.applyQualityRenderingHints()
        maskGraphics.fillRoundRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, MARGIN, MARGIN)
        maskGraphics.dispose()

        // Create masked image
        val masked = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val maskedGraphics = masked.createGraphics()
        maskedGraphics.applyQualityRenderingHints()
        maskedGraphics.drawImage(im, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null)
        maskedGraphics.composite = AlphaComposite.getInstance(AlphaComposite.DST_IN)
        maskedGraphics.drawImage(mask, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null)
        maskGraphics.dispose()

        // Draw masked background
        drawImage(masked, 0, 0, null)
    }
}