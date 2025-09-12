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
    x = -200f, // Start off-screen
    y = -200f,
    // Each enemy has its own small bullet pool.
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
        // Set a random initial delay for firing so that enemies don't all shoot in perfect sync.
        // We access the 'fireCooldown' property directly from the parent Ship class.
        super.fireCooldown = Random.nextLong(0, super.currentFireRate.delayMs)
    }

    /**
     * The single entry point for updating an enemy's state. Called by EnemyManager every frame.
     * This method is a state machine: it internally decides what logic to run.
     */
    fun update(deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        // The bullets of a destroyed ship should continue to fly. Always update them.
        updateBullets(deltaTimeMs, screenHeight)

        // Only run the "living" logic (AI, movement) if the enemy is active.
        if (isActive) {
            updateShipLogic(deltaTimeMs)
            updateMovement(deltaTimeMs, screenHeight, screenWidth)
            checkWorldBounds(screenHeight, screenWidth)
        }
    }

    /**
     * Overrides the base ship logic to provide custom enemy AI for firing.
     */
    override fun updateShipLogic(deltaTimeMs: Long) {
        // Call the parent's logic first. This handles sprite animation
        // and, most importantly, it counts down the 'fireCooldown' timer.
        super.updateShipLogic(deltaTimeMs)

        // Firing decision AI: If the cooldown is ready and we are on screen, fire.
        if (fireCooldown <= 0L && this.y > 0) {
            fire()
        }
    }

    /**
     * A private method that fully delegates movement to the current strategy.
     * There are no other movement or boundary checks in the Enemy class itself.
     */
    private fun updateMovement(deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        movementStrategy.update(this, deltaTimeMs, screenHeight, screenWidth)
    }

    /**
     * The Enemy's specialized draw method.
     * This is the key to fixing the "zombie ship" bug.
     */
    override fun draw(canvas: Canvas, paint: Paint) {
        if (isActive) {
            // If the enemy is alive, let the parent Ship class handle drawing
            // both the sprite and this enemy's active bullets.
            super.draw(canvas, paint)
        } else {
            // If the enemy has been destroyed, ONLY draw its remaining bullets.
            // Do not draw the ship's sprite.
            bulletBank.drawAll(canvas, paint)
        }
    }

    /**
     * The Enemy's specific fire command. It knows what type of bullet it should shoot.
     */
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
        movementSpeed: MovementSpeed
    ) {
        // 1. Reconfigure core properties
        this.type = newType
        this.masterBitmap = newBitmap
        this.hp = newType.hp
        this.isActive = true
        this.movementSpeed = movementSpeed.pixelsPerFrame

        // 2. Reconfigure visual state and animation
        this.clearFrames()
        val frameWidth = masterBitmap.width / newType.frameCount
        val frameHeight = masterBitmap.height
        for (i in 0 until newType.frameCount) {
            addFrameFromMaster(i, 0, frameWidth, frameHeight)
        }
        if (newType.frameCount > 1) {
            this.loops = true
            this.frameDelayMs = 250L // Could also be data-driven in EnemyType
            this.play()
        } else {
            this.pause() // Ensure non-animated sprites are paused
        }

        // 3. Reconfigure position and behavior
        this.x = startX
        this.y = startY
        this.upgradeFireRate(fireRate)
        this.movementStrategy = movementPattern

        // Reset the cooldown with a random initial delay for variety
        super.fireCooldown = Random.nextLong(fireRate.delayMs)
    }

    fun takeDamage(amount: Int) {
        if (!isActive) return
        hp -= amount
        if (hp <= 0) {
            isActive = false
        }
    }

    /**
     * Checks if the enemy has gone outside the screen boundaries.
     * If so, it wraps the enemy back to the top at a new random horizontal position.
     * This ensures no enemy is ever permanently lost off-screen.
     */
    private fun checkWorldBounds(screenHeight: Float, screenWidth: Float) {
        // Check all four boundaries
        val isOffScreenBottom = this.y > screenHeight
        val isOffScreenTop = this.y < -this.height * 2 // Give extra buffer at the top
        val isOffScreenLeft = this.x < -this.width
        val isOffScreenRight = this.x > screenWidth

        if (isOffScreenBottom || isOffScreenTop || isOffScreenLeft || isOffScreenRight) {
            // If the enemy is off-screen for ANY reason, reset it.
            this.y = -this.height // Always reset to just above the screen
            this.x = Random.nextFloat() * screenWidth // Give it a new random X start position

            // For complex strategies, we may need to reset their internal state.
            // We can add a method to the interface for this.
            movementStrategy.onOutOfBounds(this)
        }
    }
}