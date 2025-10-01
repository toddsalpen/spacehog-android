package com.spacehog.logic

import android.graphics.PointF

/**
 * Manages the conversion between a virtual design resolution and the actual device screen resolution.
 * This is the core of our screen-independent rendering system.
 */
class Scaler(
    private val actualScreenWidth: Float,
    private val actualScreenHeight: Float
) {
    // 1. We define our ideal "design" screen. Everything is planned around this size.
    // 1080p portrait is a very common and good choice.
    private val virtualWidth = 1080f
    private val virtualHeight = 1920f

    // 2. We calculate the scaling factors ONCE.
    // This tells us how to convert from virtual units to actual pixels.
    private val scaleX = actualScreenWidth / virtualWidth
    private val scaleY = actualScreenHeight / virtualHeight

    // 3. --- The Public API ---

    /** Scales a horizontal value from virtual to actual pixels. */
    fun scaleX(virtualX: Float): Float {
        return virtualX * scaleX
    }

    /** Scales a vertical value from virtual to actual pixels. */
    fun scaleY(virtualY: Float): Float {
        return virtualY * scaleY
    }

    /**
     * Scales font size. It's often best to scale text based on the width,
     * as it prevents text from becoming too tall on narrow screens.
     */
    fun scaleFont(virtualSize: Float): Float {
        return virtualSize * scaleX
    }

    /** Returns the width of a sprite based on a fraction of the screen width. */
    fun getSpriteWidth(fraction: Float): Float {
        return actualScreenWidth * fraction
    }
}