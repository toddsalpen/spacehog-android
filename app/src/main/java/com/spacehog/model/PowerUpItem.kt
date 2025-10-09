package com.spacehog.model

import android.graphics.Bitmap

/**
 * Represents a floating, collectible power-up item in the game world.
 */
class PowerUpItem(
    var type: PowerUpType,
    initialBitmap: Bitmap,
    width: Float,
    height: Float,
) : Sprite(initialBitmap, width, height, x = -200f, y = -200f) { // Start off-screen

    var isActive = false
        private set

    // A simple downward velocity
    private val velocity = Vector2(0f, 3f)

    fun spawn(newType: PowerUpType, newBitmap: Bitmap, startX: Float, startY: Float) {
        this.type = newType
        this.masterBitmap = newBitmap
        this.clearFrames()
        this.addFrameFromMaster(0, 0, masterBitmap.width, masterBitmap.height)

        // Center the item on the spawn position
        this.x = startX - (width / 2f)
        this.y = startY - (height / 2f)
        this.isActive = true
    }

    fun update(deltaTimeMs: Long, screenHeight: Float) {
        if (!isActive) return

        val normalizedDelta = deltaTimeMs / 16f
        this.y += velocity.y * normalizedDelta

        // Deactivate if it goes off the bottom of the screen
        if (y > screenHeight) {
            isActive = false
        }
    }

    fun onCollected() {
        // When collected, it deactivates itself
        isActive = false
    }
}