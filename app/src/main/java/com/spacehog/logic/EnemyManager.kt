package com.spacehog.logic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary
import com.spacehog.model.Bullet
import com.spacehog.model.Enemy
import com.spacehog.model.EnemyType
import com.spacehog.model.FireRate
import com.spacehog.model.MovementSpeed

class EnemyManager(
    private val assetLibrary: AssetLibrary,
    private val scaler: Scaler,
    private val maxEnemiesOnScreen: Int = 50
) {

    private val enemyPool = mutableListOf<Enemy>()
    private val bitmaps = mutableMapOf<EnemyType, Bitmap>()
    // --- Pre-create one of each strategy to avoid creating them during gameplay ---
    private val movementStrategies = mapOf(
        MovementPatternType.STRAIGHT_DOWN to StraightDownStrategy(),
        MovementPatternType.ZIG_ZAG to ZigZagStrategy(),
        MovementPatternType.DIAGONAL to DiagonalStrategy(),
        MovementPatternType.HUNTER to HunterStrategy(),
    )

    init {
        // Pre-fetch all needed enemy bitmaps from the library.
        for (type in EnemyType.entries) {
            bitmaps[type] = assetLibrary.getBitmap(type.asset)
        }

        // Pre-allocate the object pool.
        for (i in 0 until maxEnemiesOnScreen) {
            val defaultType = EnemyType.BLUE_BUG
            enemyPool.add(
                Enemy(
                    type = defaultType,
                    initialBitmap = bitmaps[defaultType]!!,
                    width = scaler.scaleX(defaultType.virtualWidth),
                    height = scaler.scaleY(defaultType.virtualHeight),
                    assetLibrary = assetLibrary
                )
            )
        }
    }

    fun spawnEnemy(type: EnemyType, x: Float, y: Float, fireRate: FireRate, patternType: MovementPatternType, speed: MovementSpeed) {
        val enemyToSpawn = enemyPool.firstOrNull { !it.isActive }

        val bitmap = bitmaps[type]!!
        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()

        val newWidth = scaler.scaleX(type.virtualWidth)
        val newHeight = scaler.scaleY(type.virtualHeight)


        enemyToSpawn?.spawn(
            type,
            bitmaps[type]!!,
            x,
            y,
            fireRate,
            movementStrategies[patternType]!!,
            movementSpeed = speed,
            newWidth = newWidth,
            newHeight = newHeight
            )
    }

    /**
     * Updates all enemies in the pool.
     * It now requires the list of active enemies to pass down to each enemy
     * for potential AI logic (like homing missiles for enemies in the future).
     */
    fun updateAll(deltaTimeMs: Long, screenHeight: Float, screenWidth: Float, activeEnemies: List<Enemy>) {
        for (enemy in enemyPool) {
            // We pass the list of all other active enemies to each enemy's update method.
            enemy.update(deltaTimeMs, screenHeight, screenWidth, activeEnemies)
        }
    }

    fun drawAll(canvas: Canvas, paint: Paint) {
        for (enemy in enemyPool) {
            enemy.draw(canvas, paint)
        }
    }

    fun getActiveEnemies(): List<Enemy> {
        return enemyPool.filter { it.isActive }
    }

    fun getAllEnemyBullets(): List<Bullet> {
        return enemyPool.flatMap { it.getActiveBullets() }
    }
}