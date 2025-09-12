package com.spacehog.logic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import com.spacehog.data.AssetLibrary
import com.spacehog.model.BulletType
import com.spacehog.model.Enemy
import com.spacehog.model.FireRate
import com.spacehog.model.LevelState
import com.spacehog.model.PlayerLifeState
import com.spacehog.model.PlayerShip
import com.spacehog.model.PowerUpType
import com.spacehog.model.StarField

// A utility function for smooth movement
fun lerp(start: Float, end: Float, amount: Float): Float {
    return start + amount * (end - start)
}

enum class GameWorldState {
    PLAYING,
    GAME_OVER
}

class GameWorld(context: Context, private val holder: SurfaceHolder, private val onGameOver: () -> Unit) {
    var state: GameWorldState = GameWorldState.PLAYING
        private set
    var score = 0
        private set
    val assetLibrary: AssetLibrary
    val playerShip: PlayerShip
    val starField: StarField
    private val enemyManager: EnemyManager
    val levelManager: LevelManager
    private val effectManager: EffectManager
    var lastKnownState: LevelState? = null

    init {
        Log.d("GameWorld", "Initializing GameWorld...")
        assetLibrary = AssetLibrary(context)

        val screenRect = holder.surfaceFrame

        // Bullet Pool Calculation
        val bulletSpeed = BulletType.PLAYER_STANDARD.speed.let { if(it < 0) it * -1 else it }
        val screenHeight = screenRect.height().toFloat()
        val bulletLifetimeSeconds = screenHeight / (bulletSpeed * 60)
        val bulletPoolSize = FireRate.calculateRequiredPoolSize(bulletLifetimeSeconds)

        // Player Ship Creation
        playerShip = PlayerShip(
            assetLibrary = assetLibrary,
            width = 150f,
            height = 150f,
            x = (screenRect.width() / 2f) - 75f,
            y = screenRect.height() - 250f,
            bulletPoolSize = bulletPoolSize
        )

        // StarField and Enemy Manager Creation
        starField = StarField(screenRect.width().toFloat(), screenHeight, 0f, 0f)
        starField.generateNewStars()

        enemyManager = EnemyManager(assetLibrary) // Pass assetLibrary
        levelManager = LevelManager(enemyManager)
        effectManager = EffectManager(assetLibrary)

        // Set the player's initial fire rate based on the first level's data
        playerShip.upgradeFireRate(levelManager.currentLevelProperties!!.playerFireRate)

    }

    fun update(deltaTimeMs: Long, playerTouchX: Float?, isPlayerFiring: Boolean) {

        // Pass the screen dimensions to the player and enemies.
        val screenHeight = holder.surfaceFrame.height().toFloat()
        val screenWidth = holder.surfaceFrame.width().toFloat()

        if (state == GameWorldState.GAME_OVER) return

        // Check if the player has run out of lives
        if (playerShip.lifeState == PlayerLifeState.GAME_OVER) {
            state = GameWorldState.GAME_OVER
            onGameOver() // Notify the listener that the game has ended!
            return // Stop the rest of the update for this frame
        }

        starField.update()

        // --- Player Input Logic ---
        // Movement is still tied to position
        playerShip.update(deltaTimeMs, screenHeight, screenWidth)

        playerTouchX?.let { touchX ->
            val targetX = touchX - (playerShip.width / 2f)
            playerShip.x = lerp(playerShip.x, targetX, 0.25f)
        }

        // Firing is now tied to the separate 'isFiring' flag.
        if (isPlayerFiring) {
            playerShip.fire()
        }

        // We can check if a new level has just started
        if (levelManager.state == LevelState.RUNNING && lastKnownState != LevelState.RUNNING) {
            // A new level has begun! Update the player's fire rate.
            playerShip.upgradeFireRate(levelManager.currentLevelProperties!!.playerFireRate)
        }

        lastKnownState = levelManager.state // Remember the state for the next frame
        levelManager.update(deltaTimeMs)
        enemyManager.updateAll(deltaTimeMs, screenHeight, screenWidth)
        effectManager.updateAll(deltaTimeMs)
        checkCollisions()
    }

    private fun onEnemyDefeated(enemy: Enemy) {
        // 1. Add score
        this.score += enemy.type.score
        // 2. Spawn explosion
        effectManager.spawnEffect(
            EffectType.ENEMY_EXPLOSION,
            enemy.x + enemy.width / 2f,
            enemy.y + enemy.height / 2f
        )
        levelManager.onEnemyDefeated()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        starField.draw(paint, canvas)
        enemyManager.drawAll(canvas, paint)
        playerShip.draw(canvas, paint)
        effectManager.drawAll(canvas, paint)
    }

    // Add this public method to expose the power-up queue
    fun getPowerUpQueue(): List<PowerUpType> {
        return playerShip.getPowerUpQueue()
    }

    private fun checkCollisions() {
        val activePlayerBullets = playerShip.getActiveBullets().toList()
        val activeEnemies = enemyManager.getActiveEnemies().toList()
        val activeEnemyBullets = enemyManager.getAllEnemyBullets().toList()

        // --- CHECK 1: Player Bullets vs. Enemies ---
        for (bullet in activePlayerBullets) {
            for (enemy in activeEnemies) {
                if (bullet.isActive && enemy.isActive && bullet.rectCollision(enemy)) {
                    bullet.onCollision()
                    enemy.takeDamage(bullet.type.damage)

                    // If the enemy was defeated by the bullet, call our new central method.
                    if (!enemy.isActive) {
                        onEnemyDefeated(enemy)
                    }
                }
                if (!bullet.isActive) break
            }
        }

        if (playerShip.lifeState == PlayerLifeState.ALIVE) {
            // --- CHECK 2: Enemy Bullets vs. Player ---
            for (bullet in activeEnemyBullets) {
                if (bullet.isActive && bullet.rectCollision(playerShip)) {
                    bullet.onCollision()
                    val wasDestroyed = playerShip.takeDamage(bullet.type.damage)
                    if (wasDestroyed) {
                        effectManager.spawnEffect(
                            EffectType.PLAYER_EXPLOSION,
                            playerShip.x + playerShip.width / 2f,
                            playerShip.y + playerShip.height / 2f
                        )
                        return
                    }
                }
            }

            // --- CHECK 3: Enemies vs. Player (Ramming Damage) ---
            for (enemy in activeEnemies) {
                if (enemy.isActive && enemy.rectCollision(playerShip)) {

                    // Apply damage to both parties
                    val wasPlayerDestroyed = playerShip.takeDamage(5)
                    enemy.takeDamage(5)

                    // If the ENEMY was destroyed by the collision, call our new central method.
                    if (!enemy.isActive) {
                        onEnemyDefeated(enemy)
                    }

                    // If the PLAYER was destroyed by the collision, spawn their explosion.
                    if (wasPlayerDestroyed) {
                        effectManager.spawnEffect(
                            EffectType.PLAYER_EXPLOSION,
                            playerShip.x + playerShip.width / 2f,
                            playerShip.y + playerShip.height / 2f
                        )
                        return
                    }
                }
            }

        }
    }

    fun getDebugData(): DebugData {
        return DebugData(
            levelNumber = levelManager.currentLevelNumber,
            levelState = levelManager.state,
            totalEnemiesSpawned = levelManager.totalEnemiesSpawned,
            enemiesDefeatedInLevel = levelManager.enemiesDefeatedInLevel,
            activeEnemiesOnScreen = enemyManager.getActiveEnemies().size,
            activePlayerBullets = playerShip.getActiveBullets().size,
            playerHP = playerShip.hp,
            playerLives = playerShip.lives
        )
    }
}