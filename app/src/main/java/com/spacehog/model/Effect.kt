package com.spacehog.model

import android.graphics.Bitmap
import com.spacehog.logic.EffectType

/**
 * Represents a temporary visual effect, like an explosion.
 * It's a Sprite that plays its animation once and then deactivates itself.
 */
class Effect(
    initialBitmap: Bitmap,
    width: Float,
    height: Float
) : Sprite(initialBitmap, width, height, x = -500f, y = -500f) { // Start way off-screen

    var isActive = false
        private set

    init {
        // This is a one-shot animation, so loops is false.
        this.loops = false
    }

    /**
     * "Spawns" and RECONFIGURES the effect for a new type, position, and animation.
     */
    fun spawn(
        type: EffectType,
        newBitmap: Bitmap,
        startX: Float,
        startY: Float
    ) {
        // 1. Update visual assets
        this.masterBitmap = newBitmap
        this.clearFrames()

        // 2. Re-configure animation properties from the new type
        val frameWidth = masterBitmap.width / type.frameCount
        for (i in 0 until type.frameCount) {
            addFrameFromMaster(i, 0, frameWidth, masterBitmap.height)
        }
        this.loops = false // Explosions never loop
        this.frameDelayMs = type.animDelayMs

        // 3. Set position and start the animation
        this.x = startX - (width / 2f)
        this.y = startY - (height / 2f)
        this.isActive = true
        this.resetAnimation()
        this.play()
    }


    /**
     * Updates the effect's animation.
     * Deactivates the effect once the animation is finished.
     */
    override fun update(deltaTimeMs: Long) {
        if (!isActive) return

        super.update(deltaTimeMs) // This calls the Sprite's animation logic

        // The Sprite base class sets the state to FINISHED when a non-looping animation ends.
        if (this.state == AnimationState.FINISHED) {
            this.isActive = false
        }
    }
}