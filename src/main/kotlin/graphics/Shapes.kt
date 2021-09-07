package graphics

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.Arrays.fill
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane


interface Drawable {
    fun draw(g: Graphics2D)
}

/**
 * Green dot shown for online status indicator
 */
class OnlineStatusIcon(private val size: Int, private val color: Color) : Drawable {
    override fun draw(g: Graphics2D) {
        val originalColor = g.color
        g.color = color
        g.fillOval(0, 0, size, size)
        g.color = originalColor
    }
}

/**
 * Moon icon for idle status indicator
 */
class IdleStatusIcon(size: Int, private val color: Color) : Drawable {
    private val shape: Area

    init {
        val ellipse = Ellipse2D.Float()
        ellipse.setFrame(0f, 0f, size.toFloat(), size.toFloat())
        shape = Area(ellipse)

        ellipse.setFrame(
            -.125f * size,
            -.125f * size,
            size.toFloat() * .75f,
            size.toFloat() * .75f
        )
        val outer = Area(ellipse)
        shape.subtract(outer)
    }

    override fun draw(g: Graphics2D) {
        val originalColor = g.color
        g.color = this.color
        g.fill(shape)
        g.color = originalColor
    }
}

/**
 * Ring icon shown for offline status indicator
 */
class OfflineStatusIcon(size: Int, private val color: Color) : Drawable {
    private val shape: Area

    init {
        val ellipse = Ellipse2D.Float()
        ellipse.setFrame(0f, 0f, size.toFloat(), size.toFloat())
        shape = Area(ellipse)

        ellipse.setFrame(
            .25f * size,
            .25f * size,
            size.toFloat() * .5f,
            size.toFloat() * .5f
        )
        val inner = Area(ellipse)
        shape.subtract(inner)
    }

    override fun draw(g: Graphics2D) {
        val originalColor = g.color
        g.color = color
        g.fill(shape)
        g.color = originalColor
    }
}

/**
 * Ring icon shown for "do no disturb" status indicator
 */
class DndStatusIcon(size: Int, private val color: Color) : Drawable {
    private val shape: Area

    init {
        val ellipse = Ellipse2D.Float()
        ellipse.setFrame(0f, 0f, size.toFloat(), size.toFloat())
        shape = Area(ellipse)

        val innerRect = RoundRectangle2D.Float(
            .125f * size,
            .375f * size,
            size.toFloat() * .75f,
            size.toFloat() * .25f,
            .25f * size,
            .25f * size
        )
        val inner = Area(innerRect)
        shape.subtract(inner)
    }

    override fun draw(g: Graphics2D) {
        val originalColor = g.color
        g.color = color
        g.fill(shape)
        g.color = originalColor
    }
}

class AvatarMask(size: Int) : Drawable {

    private val shape: Area

    init {
        val ellipse = Ellipse2D.Float(0f, 0f, size.toFloat(), size.toFloat())
        shape = Area(ellipse)

        val pos = size - (size * .333f)

        val bottomDot = Ellipse2D.Float(pos, pos, size * .333f, size * .333f)
        shape.subtract(Area(bottomDot))
    }

    override fun draw(g: Graphics2D) {
        g.fill(shape)
    }
}

/**
 * The frame on the left of the avatar
 */
class AvatarFrame(
    height: Int,
    width: Int,
    margin: Int,
    cornerRadius: Int,
) : Drawable {

    private val shape: Area

    init {
        val rect = RoundRectangle2D.Float(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            cornerRadius.toFloat(),
            cornerRadius.toFloat()
        )
        shape = Area(rect)

        // Remove round corners from right side
        shape.add(Area(
            Rectangle2D.Float(
                width - cornerRadius.toFloat(),
                0f,
                cornerRadius.toFloat(),
                height.toFloat()
            )
        ))

        // Cut out a circle with size equal to the rectangle's height minus two times the margin
        val innerCircleSize = height - (margin * 2f)
        val x = width - innerCircleSize / 2f
        val y = (height - innerCircleSize) / 2f
        val innerCircle = Ellipse2D.Float(
            x,
            y,
            innerCircleSize,
            innerCircleSize
        )
        shape.subtract(Area(innerCircle))
    }
    override fun draw(g: Graphics2D) {
        (g.create() as Graphics2D).fill(shape)
    }
}

fun Graphics2D.draw(drawable: Drawable) = drawable.draw(this)
