package com.spacehog.model

import android.graphics.Bitmap

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

    var isActive = false
        private set

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
    }

    /**
     * Updates the bullet's position based on its type's speed.
     */
    fun update(deltaTimeMs: Long, screenHeight: Float) {
        if (!isActive) return

        // Normalize movement to a 60fps baseline for consistency
        val normalizedDelta = deltaTimeMs / 16f
        y += type.speed * normalizedDelta

        // Deactivate if it goes off the top or bottom of the screen
        if (y < -height || y > screenHeight) {
            isActive = false
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