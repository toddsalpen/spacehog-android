package com.spacehog.logic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.ui.geometry.Offset
import com.spacehog.data.AssetLibrary
import com.spacehog.model.BulletType
import com.spacehog.model.Enemy
import com.spacehog.model.FireRate
import com.spacehog.model.LevelState
import com.spacehog.model.PlayerLifeState
import com.spacehog.model.PlayerShip
import com.spacehog.model.PlayerState
import com.spacehog.model.PowerUpType
import com.spacehog.model.StarField
import kotlin.random.Random

// A utility function for smooth movement
fun lerp(start: Float, end: Float, amount: Float): Float {
    return start + amount * (end - start)
}

enum class GameWorldState {
    PLAYING,
    GAME_OVER
}

class GameWorld(
    context: Context,
    private val holder: SurfaceHolder,
    private val scaler: Scaler,
    val starfieldManager: StarfieldManager,
    private val onGameOver: () -> Unit
){

    val assetLibrary: AssetLibrary
    val playerShip: PlayerShip
    val enemyManager: EnemyManager
    val effectManager: EffectManager
    val levelManager: LevelManager

    var state: GameWorldState = GameWorldState.PLAYING
        private set
    var score = 0
        private set
    var lastKnownState: LevelState? = null

    val powerUpManager: PowerUpManager
    private var lastKnownLevel = 0

    init {
        Log.d("GameWorld", "Initializing GameWorld...")
        assetLibrary = AssetLibrary(context)
        val screenRect = holder.surfaceFrame
        val screenHeight = holder.surfaceFrame.height().toFloat()
        val screenWidth = holder.surfaceFrame.width().toFloat()
        val bulletSpeed = BulletType.PLAYER_STANDARD.speed.let { if(it < 0) it * -1 else it }
        val bulletLifetimeSeconds = screenHeight / (bulletSpeed * 60)
        val bulletPoolSize = FireRate.calculateRequiredPoolSize(bulletLifetimeSeconds)

        val playerBitmap = assetLibrary.getBitmap(PlayerState.NORMAL.asset)
        val playerAspectRatio = playerBitmap.height.toFloat() / playerBitmap.width.toFloat()
        val playerWidth = scaler.getSpriteWidth(1f / 8f)
        val playerHeight = playerWidth * playerAspectRatio

        playerShip = PlayerShip(
            assetLibrary = assetLibrary,
            width = playerWidth,
            height = playerHeight,
            x = (screenRect.width() / 2f) - (playerWidth / 2f),
            y = screenRect.height() - scaler.scaleY(300f),
            bulletPoolSize = bulletPoolSize
        )

        enemyManager = EnemyManager(assetLibrary, scaler)
        levelManager = LevelManager(enemyManager, scaler)
        effectManager = EffectManager(assetLibrary)
        powerUpManager = PowerUpManager(assetLibrary, scaler)

        syncPlayerToLevel()

    }

    fun update(deltaTimeMs: Long, playerTouchPosition: Offset?, isPlayerFiring: Boolean) {
        if (state == GameWorldState.GAME_OVER) return

        levelManager.update(deltaTimeMs)

        if (isPlayerFiring) {
            playerShip.fire()
        }

        playerTouchPosition?.let { touch ->// Calculate the target X position (centered on the touch)
            val targetX = touch.x - (playerShip.width / 2f)
            // Calculate the target Y position (with a vertical offset so the ship is below the finger)
            val targetY = touch.y - (playerShip.height * 1.5f)
            playerShip.x = lerp(playerShip.x, targetX, 0.25f)
            playerShip.y = lerp(playerShip.y, targetY, 0.25f)
        }

        playerShip.update(deltaTimeMs, holder.surfaceFrame.height().toFloat(), holder.surfaceFrame.width().toFloat())
        powerUpManager.updateAll(deltaTimeMs, holder.surfaceFrame.height().toFloat())
        enemyManager.updateAll(deltaTimeMs, holder.surfaceFrame.height().toFloat(), holder.surfaceFrame.width().toFloat())
        effectManager.updateAll(deltaTimeMs)

        checkCollisions()

        if (playerShip.lifeState == PlayerLifeState.GAME_OVER) {
            state = GameWorldState.GAME_OVER
            onGameOver()
            return
        }

        if (levelManager.currentLevelNumber > lastKnownLevel) {
            syncPlayerToLevel()
        }
    }

    private fun onEnemyDefeated(enemy: Enemy) {
        this.score += enemy.type.score
        effectManager.spawnEffect(
            EffectType.ENEMY_EXPLOSION,
            enemy.x + enemy.width / 2f,
            enemy.y + enemy.height / 2f
        )
        levelManager.onEnemyDefeated()
        if (Random.nextInt(100) < 25) {
            // Randomly select which power-up to drop
            val randomPowerUp = PowerUpType.entries.random()
            powerUpManager.spawnItem(
                randomPowerUp,
                enemy.x + enemy.width / 2f,
                enemy.y + enemy.height / 2f
            )
        }
    }

    fun getPowerUpQueue(): List<PowerUpType> {
        return playerShip.getPowerUpQueue()
    }

    private fun checkCollisions() {
        val activePlayerBullets = playerShip.getActiveBullets().toList()
        val activeEnemies = enemyManager.getActiveEnemies().toList()
        val activeEnemyBullets = enemyManager.getAllEnemyBullets().toList()
        val activePowerUpItems = powerUpManager.getActiveItems().toList()

        for (bullet in activePlayerBullets) {
            for (enemy in activeEnemies) {
                if (bullet.isActive && enemy.isActive && bullet.rectCollision(enemy)) {
                    bullet.onCollision()
                    enemy.takeDamage(bullet.type.damage)

                    if (!enemy.isActive) {
                        onEnemyDefeated(enemy)
                    }
                }
                if (!bullet.isActive) break
            }
        }

        if (playerShip.lifeState == PlayerLifeState.ALIVE) {
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

            for (enemy in activeEnemies) {
                if (enemy.isActive && enemy.rectCollision(playerShip)) {

                    val wasPlayerDestroyed = playerShip.takeDamage(5)
                    enemy.takeDamage(5)

                    if (!enemy.isActive) {
                        onEnemyDefeated(enemy)
                    }

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

            for (item in activePowerUpItems) {
                // Check if the player ship is touching a power-up item
                if (item.isActive && item.rectCollision(playerShip)) {
                    // Tell the ship it collected the item
                    playerShip.collectPowerUp(item.type)
                    // Tell the item it has been collected so it disappears
                    item.onCollected()
                }
            }

        }
    }

    private fun syncPlayerToLevel() {
        levelManager.currentLevelProperties?.let { properties ->
            playerShip.upgradeFireRate(properties.playerFireRate)
        }
        lastKnownLevel = levelManager.currentLevelNumber
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