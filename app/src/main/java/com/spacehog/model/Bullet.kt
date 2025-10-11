package com.spacehog.model

import android.graphics.Bitmap
import com.spacehog.data.GameAsset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a single projectile, configured by a BulletType.
 */
class Bullet(
    // It's created with an initial type and bitmap
    var type: BulletType,
    initialBitmap: Bitmap,
    width: Float,
    height: Float
) : Sprite(initialBitmap, width, height, x = -100f, y = -100f) { // Start off-screen

    val velocity = Vector2(0f, 0f)
    var isActive = false
        private set
    // A missile needs a target
    private var target: Enemy? = null

    /**
     * "Spawns" a bullet, reconfiguring it for a new type, bitmap, and position.
     */
    fun spawn(
        newType: BulletType,
        newBitmap: Bitmap,
        startX: Float,
        startY: Float
    ) {
        this.type = newType
        // Update the visual representation by changing the master bitmap and regenerating the frame
        this.masterBitmap = newBitmap
        this.clearFrames()
        this.addFrameFromMaster(0, 0, masterBitmap.width, masterBitmap.height)

        // Update position and physics from the new type
        this.x = startX - (width / 2f)
        this.y = startY
        this.isActive = true
        // Missiles start by flying straight up
        this.velocity.set(0f, newType.speed)
        this.target = null // Clear any old target

        if (newType.asset == GameAsset.PLAYER_BULLET || newType.asset == GameAsset.PLAYER_MISSILE) {
            // Player projectiles go UP (-Y direction)
            this.velocity.set(0f, -kotlin.math.abs(newType.speed))
        } else {
            // Enemy projectiles go DOWN (+Y direction)
            this.velocity.set(0f, kotlin.math.abs(newType.speed))
        }
    }

    /**
     * Updates the bullet's position based on its type's speed.
     */
    fun update(deltaTimeMs: Long, screenHeight: Float, activeEnemies: List<Enemy>) {
        if (!isActive) return

        // Normalize movement to a 60fps baseline for consistency
        val normalizedDelta = deltaTimeMs / 16f

        // --- HOMING MISSILE LOGIC ---
        if (type.isHoming) {
            if (target == null || !target!!.isActive) {
                target = findClosestEnemy(activeEnemies)
            }
            target?.let { currentTarget ->
                // Calculate direction to target
                val directionToTargetX = currentTarget.x + currentTarget.width / 2f - this.x
                val directionToTargetY = currentTarget.y + currentTarget.height / 2f - this.y

                // Get the angle to the target in degrees
                val targetAngle = Math.toDegrees(atan2(directionToTargetY.toDouble(), directionToTargetX.toDouble())).toFloat()

                // Get the missile's current angle
                val currentAngle = Math.toDegrees(atan2(velocity.y.toDouble(), velocity.x.toDouble())).toFloat()

                // Find the difference and clamp it to the turn rate
                var angleDifference = targetAngle - currentAngle
                // Handle wrapping around 360 degrees
                if (angleDifference > 180) angleDifference -= 360
                if (angleDifference < -180) angleDifference += 360

                val turnAmount = angleDifference.coerceIn(-type.turnRate * normalizedDelta, type.turnRate * normalizedDelta)

                // Apply the turn to our current angle
                val newAngle = currentAngle + turnAmount

                // Update velocity based on the new angle and speed
                val absoluteSpeed = kotlin.math.abs(type.speed)
                velocity.x = cos(Math.toRadians(newAngle.toDouble())).toFloat() * absoluteSpeed
                velocity.y = sin(Math.toRadians(newAngle.toDouble())).toFloat() * absoluteSpeed
            }
        }

        // Apply velocity to position for ALL bullet types
        this.x += velocity.x * normalizedDelta
        this.y += velocity.y * normalizedDelta

        // Update rotation of the sprite to match its velocity vector
        this.rotation = Math.toDegrees(atan2(velocity.y.toDouble(), velocity.x.toDouble())).toFloat() + 90f

        // Deactivation logic (unchanged)

        // Deactivate if it goes off the top or bottom of the screen
        if (y < -height || y > screenHeight) {
            isActive = false
        }
    }

    // --- NEW HELPER FOR FINDING TARGETS ---
    private fun findClosestEnemy(enemies: List<Enemy>): Enemy? {
        return enemies.minByOrNull { enemy ->
            // Use squared distance for performance (avoids sqrt)
            val dx = (enemy.x + enemy.width / 2f) - this.x
            val dy = (enemy.y + enemy.height / 2f) - this.y
            dx * dx + dy * dy
        }
    }

    /**
     * Handles the bullet's logic when it collides with an object.
     */
    fun onCollision() {
        // If this bullet's type is not a piercing shot, it deactivates itself.
        if (!type.pierces) {
            isActive = false
        }
        // If it IS a piercing shot, this method does nothing, and the bullet keeps going.
    }
}