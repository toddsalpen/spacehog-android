package com.spacehog.model

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

/**
 * Represents a single star in the StarField.
 *
 * This class is highly optimized for game loop usage. It inherits its position
 * and dimensions from GameObject and contains its own logic for movement and drawing.
 *
 * @param vy The initial vertical velocity of the star.
 * @param layer The parallax layer the star belongs to (deeper layers move slower).
 */
class Star(
    width: Float,
    height: Float,
    x: Float,
    y: Float,
    vx: Float,
    vy: Float,
    val layer: Int
) : GameObject(width, height, x, y) {

    private val velocity: Vector2
    private val starType: StarType

    // --- State Properties ---
    private val lifeSpan = 20f
    private var life = lifeSpan
    private val decay = 0.1f // How quickly the star 'fades'
    var active = true
        private set // Can be read from outside, but only set from within this class

    /**
     * Defines the possible brightness levels (alpha values) of a star.
     */
    enum class StarType(val alpha: Int) {
        // Deepest layer stars (dimmest)
        STAR_1(50),
        STAR_2(70),
        // Mid-layer stars
        STAR_3(110),
        STAR_4(130),
        // Foreground stars (brightest)
        STAR_5(170),
        STAR_6(200),
        STAR_7(255);

        companion object {
            // Pre-cache lists for high-performance random selection.
            private val deepStars = listOf(STAR_1, STAR_2)
            private val midStars = listOf(STAR_3, STAR_4)
            private val frontStars = listOf(STAR_5, STAR_6, STAR_7)

            /**
             * Gets a random StarType appropriate for the given layer.
             * This is much safer and more efficient than the original recursive method.
             */
            fun randomForLayer(layer: Int): StarType {
                return when (layer) {
                    2 -> deepStars.random() // Layer 2 is the furthest back
                    1 -> midStars.random()
                    else -> frontStars.random() // Layer 0 is the closest
                }
            }
        }
    }

    init {
        velocity = Vector2(vx, vy)
        // BEST PRACTICE: Consolidate complex logic into the StarType enum.
        // This makes the Star constructor much cleaner.
        starType = StarType.randomForLayer(layer)

        // Adjust velocity based on the layer to create a parallax effect.
        when (layer) {
            1 -> velocity.multiply(0.6f)
            2 -> velocity.multiply(0.25f)
        }

        // Ensure stars have a minimum speed.
        if (velocity.y < 1f) {
            velocity.y = 1f
        }
    }

    /**
     * Resets the star to a new random position at the top of the screen,
     * restoring its life and active state.
     *
     * @param screenWidth The width of the screen for random placement.
     */
    fun resetStar(screenWidth: Float, screenHeight: Float) {
        // BEST PRACTICE: Use the shared, efficient Random.Default.
        this.x = Random.nextInt(screenWidth.toInt()).toFloat()
        this.y = -this.height // Place just off-screen at the top
        this.active = true
        this.life = lifeSpan
    }

    /**
     * Updates the star's position and lifecycle state for the next frame.
     */
    fun update() {
        if (!active) return

        x += velocity.x
        y += velocity.y

        // The lifespan logic was present but unused in the original. Now it works.
        life -= decay
        if (life <= 0f) {
            active = false
        }
    }

    /**
     * Draws the star onto the canvas if it's active.
     *
     * IMPORTANT: This method only sets the necessary paint properties (alpha) for
     * this specific object. It does NOT call paint.reset().
     */
    fun draw(paint: Paint, canvas: Canvas) {
        if (active) {
            // Set the star's brightness (alpha). The renderer sets the base color (white).
            paint.alpha = starType.alpha
            // Use the computed `rect` property from the GameObject base class. It is always correct.
            canvas.drawRect(rect, paint)
        }
    }
}