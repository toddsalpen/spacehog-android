package com.spacehog.logic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary
import com.spacehog.data.GameAsset
import com.spacehog.model.Effect

// We can define our effect types here for now.
enum class EffectType(
    val asset: GameAsset,
    val frameCount: Int,
    val animDelayMs: Long
) {
    ENEMY_EXPLOSION(GameAsset.EXPLOSION_ENEMY, frameCount = 5, animDelayMs = 50L),
    PLAYER_EXPLOSION(GameAsset.EXPLOSION_PLAYER, frameCount = 4, animDelayMs = 75L);
}

class EffectManager(
    private val assetLibrary: AssetLibrary,
    private val maxEffectsOnScreen: Int = 30
) {
    private val effectPool = mutableListOf<Effect>()
    private val bitmaps = mutableMapOf<EffectType, Bitmap>()

    init {
        // Pre-load all effect bitmaps
        for (type in EffectType.entries) {
            bitmaps[type] = assetLibrary.getBitmap(type.asset)
        }

        // Pre-allocate the object pool
        for (i in 0 until maxEffectsOnScreen) {
            val defaultType = EffectType.ENEMY_EXPLOSION
            effectPool.add(
                Effect(
                    initialBitmap = bitmaps[defaultType]!!,
                    width = 150f,
                    height = 150f
                )
            )
        }
    }

    /**
     * Finds an inactive effect and tells it to respawn as the correct type.
     */
    fun spawnEffect(type: EffectType, x: Float, y: Float) {
        effectPool.firstOrNull { !it.isActive }?.spawn(
            type = type, // Pass the type
            newBitmap = bitmaps[type]!!, // Pass the correct bitmap
            startX = x,
            startY = y
        )
    }

    fun updateAll(deltaTimeMs: Long) {
        for (effect in effectPool) {
            if (effect.isActive) {
                effect.update(deltaTimeMs)
            }
        }
    }

    fun drawAll(canvas: Canvas, paint: Paint) {
        for (effect in effectPool) {
            if (effect.isActive) {
                effect.draw(canvas, paint)
            }
        }
    }
}