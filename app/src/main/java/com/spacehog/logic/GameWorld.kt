package com.spacehog.logic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import com.spacehog.model.PlayerState
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
        val playerWidth = scaler.getSpriteWidth(1f / 7f)
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

        //playerShip.upgradeFireRate(levelManager.currentLevelProperties!!.playerFireRate)

        syncPlayerToLevel()

    }

    fun update(deltaTimeMs: Long, playerTouchX: Float?, isPlayerFiring: Boolean) {
        if (state == GameWorldState.GAME_OVER) return

        levelManager.update(deltaTimeMs)

        if (isPlayerFiring) {
            playerShip.fire()
        }

        playerTouchX?.let { touchX ->
            val targetX = touchX - (playerShip.width / 2f)
            playerShip.x = lerp(playerShip.x, targetX, 0.25f)
        }

        playerShip.update(deltaTimeMs, holder.surfaceFrame.height().toFloat(), holder.surfaceFrame.width().toFloat())
        enemyManager.updateAll(deltaTimeMs, holder.surfaceFrame.height().toFloat(), holder.surfaceFrame.width().toFloat())
        effectManager.updateAll(deltaTimeMs)

        checkCollisions()

        if (playerShip.lifeState == PlayerLifeState.GAME_OVER) {
            state = GameWorldState.GAME_OVER
            onGameOver()
            return
        }

//        if (levelManager.state == LevelState.RUNNING && lastKnownState != LevelState.RUNNING) {
//            playerShip.upgradeFireRate(levelManager.currentLevelProperties!!.playerFireRate)
//        }
//
//        if (levelManager.currentLevelNumber > lastKnownLevel) {
//            syncPlayerToLevel()
//        }
//
//        lastKnownState = levelManager.state
        //---------------
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
    }

    fun getPowerUpQueue(): List<PowerUpType> {
        return playerShip.getPowerUpQueue()
    }

    private fun checkCollisions() {
        val activePlayerBullets = playerShip.getActiveBullets().toList()
        val activeEnemies = enemyManager.getActiveEnemies().toList()
        val activeEnemyBullets = enemyManager.getAllEnemyBullets().toList()

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

        }
    }

    private fun syncPlayerToLevel() {
        levelManager.currentLevelProperties?.let { properties ->
            // Update the player ship with the rules from the new level data.
            playerShip.upgradeFireRate(properties.playerFireRate)
            // In the future, you could add more things here:
            // playerShip.setSpeed(properties.playerSpeed)
        }
        // Update our tracker so this only runs once per level.
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