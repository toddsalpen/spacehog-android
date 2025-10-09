package com.spacehog.logic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary
import com.spacehog.model.PowerUpItem
import com.spacehog.model.PowerUpType

class PowerUpManager(
    private val assetLibrary: AssetLibrary,
    private val scaler: Scaler
) {
    private val itemPool = mutableListOf<PowerUpItem>()
    private val bitmaps = mutableMapOf<PowerUpType, Bitmap>()
    private val maxItemsOnScreen = 10 // Pool size

    init {
        // Pre-load all power-up item assets
        for (type in PowerUpType.entries) {
            bitmaps[type] = assetLibrary.getBitmap(type.itemAsset)
        }

        // Pre-allocate the pool
        val defaultType = PowerUpType.DOUBLE_SHOT
        for (i in 0 until maxItemsOnScreen) {
            itemPool.add(
                PowerUpItem(
                    type = defaultType,
                    initialBitmap = bitmaps[defaultType]!!,
                    width = scaler.scaleX(80f), // Scaled size for items
                    height = scaler.scaleX(80f)
                )
            )
        }
    }

    fun spawnItem(type: PowerUpType, x: Float, y: Float) {
        val itemToSpawn = itemPool.firstOrNull { !it.isActive }
        itemToSpawn?.spawn(
            newType = type,
            newBitmap = bitmaps[type]!!,
            startX = x,
            startY = y
        )
    }

    fun updateAll(deltaTimeMs: Long, screenHeight: Float) {
        for (item in itemPool) {
            if (item.isActive) {
                item.update(deltaTimeMs, screenHeight)
            }
        }
    }

    fun drawAll(canvas: Canvas, paint: Paint) {
        for (item in itemPool) {
            if (item.isActive) {
                item.draw(canvas, paint)
            }
        }
    }

    fun getActiveItems(): List<PowerUpItem> {
        return itemPool.filter { it.isActive }
    }
}