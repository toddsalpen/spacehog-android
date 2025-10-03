package com.spacehog.logic

import com.spacehog.ui.common.StarState
import kotlin.random.Random

/**
 * The single source of truth for managing the state of a starfield background.
 * This is a PURE logic class with no knowledge of how to draw. It is platform-agnostic.
 */
class StarfieldManager {

    private val stars = mutableListOf<StarState>()
    val starStates: List<StarState> = stars // The public, read-only state for renderers to use

    private var screenWidth = 0f
    private var screenHeight = 0f

    fun initialize(width: Float, height: Float, starCount: Int = 150) {
        if (stars.isNotEmpty()) return
        screenWidth = width
        screenHeight = height

        for (i in 0 until starCount) {
            stars.add(
                StarState(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    speed = 1f + Random.nextFloat() * 4f,
                    alpha = Random.nextFloat()
                )
            )
        }
    }

    fun update() {
        if (screenHeight == 0f) return

        for (star in stars) {
            star.y += star.speed
            if (star.y > screenHeight) {
                star.y = 0f
                star.x = Random.nextFloat() * screenWidth
            }
        }
    }
}