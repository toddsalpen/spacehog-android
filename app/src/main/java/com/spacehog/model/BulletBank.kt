package com.spacehog.model

import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary

class BulletBank(
    poolSize: Int,
    private val assetLibrary: AssetLibrary // It now holds a reference to the central library
) {
    val allBullets: List<Bullet>

    init {
        // Pre-allocate the entire pool of bullets with a default type.
        // This will be reconfigured when fired.
        allBullets = List(poolSize) {
            Bullet(
                type = BulletType.PLAYER_STANDARD, // Default type
                initialBitmap = assetLibrary.getBitmap(BulletType.PLAYER_STANDARD.asset),
                width = 10f,  // Standard bullet size
                height = 30f
            )
        }
    }

    /**
     * Finds an inactive bullet and respawns it with a specific type and position.
     */
    fun fire(type: BulletType, startX: Float, startY: Float) {
        val bulletToFire = allBullets.firstOrNull { !it.isActive }
        bulletToFire?.spawn(
            newType = type,
            newBitmap = assetLibrary.getBitmap(type.asset),
            startX = startX,
            startY = startY
        )
    }

    fun updateAll(deltaTimeMs: Long, screenHeight: Float) {
        for (bullet in allBullets) {
            if (bullet.isActive) {
                bullet.update(deltaTimeMs, screenHeight)
            }
        }
    }

    fun drawAll(canvas: Canvas, paint: Paint) {
        for (bullet in allBullets) {
            if (bullet.isActive) {
                bullet.draw(canvas, paint)
            }
        }
    }
}