package com.spacehog.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary
import com.spacehog.logic.MovementStrategy
import com.spacehog.logic.StraightDownStrategy
import kotlin.random.Random

class Enemy(
    var type: EnemyType,
    initialBitmap: Bitmap,
    width: Float,
    height: Float,
    assetLibrary: AssetLibrary
) : Ship(
    bitmap = initialBitmap,
    width = width,
    height = height,
    x = -200f,
    y = -200f,
    bulletBank = BulletBank(poolSize = 5, assetLibrary = assetLibrary)
) {
    var hp = type.hp
    var isActive = false
        private set

    var movementStrategy: MovementStrategy = StraightDownStrategy()
        private set

    var movementSpeed: Float = MovementSpeed.NORMAL.pixelsPerFrame
        private set

    init {
        // Set an initial random fire cooldown for variety.
        // This will be properly configured again upon spawn.
        super.fireCooldown = Random.nextLong(0, super.currentFireRate.delayMs)
    }

    /**
     * This is the single entry point for updating an enemy's state, called by EnemyManager.
     * It correctly handles the "disappearing bullet" bug.
     */
    fun update(deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        // --- THIS IS THE PERSISTENT LOGIC ---
        // An enemy's bullets should continue to update even if the enemy is destroyed.
        updateBullets(deltaTimeMs, screenHeight)

        // --- THIS IS THE "LIVING" LOGIC ---
        // Only run AI (movement, firing) if the enemy is active.
        if (isActive) {
            // updateShipLogic handles animation and firing cooldowns
            super.updateShipLogic(deltaTimeMs)

            // Firing Decision AI
            if (fireCooldown <= 0L && this.y > 0) {
                fire()
            }

            // Movement AI and Boundary Checks
            movementStrategy.update(this, deltaTimeMs, screenHeight, screenWidth)
            checkWorldBounds(screenHeight, screenWidth)
        }
    }

    // THIS IS NO LONGER NEEDED. updateShipLogic is now only defined in the parent Ship class.
    // override fun updateShipLogic(deltaTimeMs: Long) { ... }

    // THIS IS NO LONGER NEEDED. The logic is now inside the main update method.
    // private fun updateMovement(...) { ... }

    /**
     * The Enemy's specialized draw method to handle invisibility when defeated.
     */
    override fun draw(canvas: Canvas, paint: Paint) {
        if (isActive) {
            super.draw(canvas, paint)
        } else {
            bulletBank.drawAll(canvas, paint)
        }
    }

    fun fire() {
        super.fireWeapon(BulletType.ENEMY_STANDARD)
    }

    /**
     * "Spawns" and completely reconfigures an enemy from the object pool.
     */
    fun spawn(
        newType: EnemyType,
        newBitmap: Bitmap,
        startX: Float,
        startY: Float,
        fireRate: FireRate,
        movementPattern: MovementStrategy,
        movementSpeed: MovementSpeed,
        newWidth: Float,
        newHeight: Float
    ) {
        this.type = newType
        this.masterBitmap = newBitmap
        this.hp = newType.hp
        this.isActive = true
        this.width = newWidth
        this.height = newHeight

        this.clearFrames()

        val frameWidth = masterBitmap.width / newType.frameCount
        val frameHeight = masterBitmap.height
        for (i in 0 until newType.frameCount) {
            addFrameFromMaster(i, 0, frameWidth, frameHeight)
        }
        if (newType.frameCount > 1) {
            this.loops = true
            this.frameDelayMs = 250L
            this.play()
        } else {
            this.pause()
        }

        this.x = startX
        this.y = startY
        this.upgradeFireRate(fireRate)
        this.movementStrategy = movementPattern
        this.movementSpeed = movementSpeed.pixelsPerFrame
        super.fireCooldown = Random.nextLong(fireRate.delayMs)
    }

    fun takeDamage(amount: Int) {
        if (!isActive) return
        hp -= amount
        if (hp <= 0) {
            isActive = false
        }
    }

    private fun checkWorldBounds(screenHeight: Float, screenWidth: Float) {
        val isOffScreenBottom = this.y > screenHeight
        val isOffScreenTop = this.y < -this.height * 2
        val isOffScreenLeft = this.x < -this.width
        val isOffScreenRight = this.x > screenWidth

        if (isOffScreenBottom || isOffScreenTop || isOffScreenLeft || isOffScreenRight) {
            this.y = -this.height
            this.x = Random.nextFloat() * screenWidth
            movementStrategy.onOutOfBounds(this)
        }
    }
}