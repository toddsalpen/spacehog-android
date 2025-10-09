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
    open fun fireWeapon(
        bulletType: BulletType,
        fireX: Float = this.x + (this.width / 2f), // Default to ship center
        fireY: Float = this.y // Default to ship front
    ) {
        if (fireCooldown <= 0L) {
            bulletBank.fire(bulletType, fireX, fireY)
            fireCooldown = currentFireRate.delayMs
        }
    }

    // The single, powerful fire method. It does NOT check cooldown.
    fun fireProjectile(bulletType: BulletType, fireX: Float, fireY: Float) {
        bulletBank.fire(bulletType, fireX, fireY)
    }

    // A method to check if the weapon is ready to fire.
    fun canFire(): Boolean {
        return fireCooldown <= 0L
    }

    // A method to reset the cooldown AFTER firing is complete.
    fun resetFireCooldown() {
        fireCooldown = currentFireRate.delayMs
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