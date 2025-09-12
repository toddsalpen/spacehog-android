package com.spacehog.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

// The base Ship class is a Sprite that HAS a BulletBank.
open class Ship(
    bitmap: Bitmap,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
    protected val bulletBank: BulletBank
) : Sprite(bitmap, width, height, x, y) {

    var currentFireRate: FireRate = FireRate.TARDY
    var fireCooldown = 0L

    open fun updateShipLogic(deltaTimeMs: Long) {
        super.update(deltaTimeMs) // Handles sprite animation
        if (fireCooldown > 0) {
            fireCooldown -= deltaTimeMs
        }
    }

    fun updateBullets(deltaTimeMs: Long, screenHeight: Float) {
        bulletBank.updateAll(deltaTimeMs, screenHeight)
    }

    // The base fire method is now more generic.
    open fun fireWeapon(bulletType: BulletType) {
        if (fireCooldown <= 0L) {
            val firePositionX = this.x + (this.width / 2f)
            val firePositionY = this.y
            bulletBank.fire(bulletType, firePositionX, firePositionY)
            fireCooldown = currentFireRate.delayMs
        }
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        super.draw(canvas, paint)
        bulletBank.drawAll(canvas, paint)
    }

    fun getActiveBullets(): List<Bullet> {
        return bulletBank.allBullets.filter { it.isActive }
    }

    fun upgradeFireRate(newRate: FireRate) {
        currentFireRate = newRate
    }
}