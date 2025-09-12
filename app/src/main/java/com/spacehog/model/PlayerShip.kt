package com.spacehog.model

import android.graphics.Canvas
import android.graphics.Paint
import com.spacehog.data.AssetLibrary
import com.spacehog.data.GameAsset
import com.spacehog.logic.PiercingGun
import com.spacehog.logic.StandardGun
import com.spacehog.logic.WeaponSystem
import java.util.LinkedList
import java.util.Queue

// This now becomes the SINGLE source of truth for the player's state.
// It controls both LOGIC and VISUALS.
enum class PlayerLifeState(val asset: GameAsset) {
    ALIVE(GameAsset.PLAYER_SHIP),
    CAPTURED(GameAsset.CAPTURED_SHIP), // A captured player is visually different
    EXPLODING(GameAsset.PLAYER_SHIP), // State is used to trigger an effect
    RESPAWNING(GameAsset.PLAYER_SHIP), // The player is invisible in this state
    GAME_OVER(GameAsset.PLAYER_SHIP)
}

class PlayerShip(
    private val assetLibrary: AssetLibrary, // Holds a reference to the asset library
    width: Float,
    height: Float,
    x: Float,
    y: Float,
    bulletPoolSize: Int
) : Ship(
    // Initial visual state
    bitmap = assetLibrary.getBitmap(PlayerState.NORMAL.asset),
    width = width,
    height = height,
    x = x,
    y = y,
    // It creates and passes its OWN BulletBank to the parent constructor.
    bulletBank = BulletBank(poolSize = bulletPoolSize, assetLibrary = assetLibrary)
) {

    var lives = 3
    var maxHp = 5
    var hp = maxHp
    var lifeState = PlayerLifeState.ALIVE
        private set

    // The isAlive check is now more comprehensive
    val isAlive: Boolean
        get() = lifeState == PlayerLifeState.ALIVE || lifeState == PlayerLifeState.CAPTURED

    // --- RE-SPAWN TIME ---
    private val respawnTimeMs = 2000L
    private var respawnTimer = 0L
    // --- RE-SPAWN ---

    // --- POWER-UP AND WEAPON SYSTEM ---
    private val powerUpQueue: Queue<PowerUpType> = LinkedList()
    private var activePowerUp: PowerUpType? = null
    private var powerUpTimerMs = 0L
    private var currentWeapon: WeaponSystem = StandardGun()
    // --- END ---

    private val startY: Float = y

    fun update(deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        when (lifeState) {
            PlayerLifeState.ALIVE -> {
                if (activePowerUp != null) {
                    powerUpTimerMs -= deltaTimeMs
                    if (powerUpTimerMs <= 0L) {
                        activePowerUp = null
                        currentWeapon = StandardGun()
                    }
                }
                updateShipLogic(deltaTimeMs)
                updateBullets(deltaTimeMs, screenHeight)
            }
            PlayerLifeState.CAPTURED -> {
                // Future logic for when a ship is captured
                // It would likely move with its captor and not be controllable
                updateBullets(deltaTimeMs, screenHeight) // Still update lingering bullets
            }
            PlayerLifeState.EXPLODING -> {
                respawnTimer = respawnTimeMs
                lifeState = PlayerLifeState.RESPAWNING
                updateBullets(deltaTimeMs, screenHeight)
            }
            PlayerLifeState.RESPAWNING -> {
                updateBullets(deltaTimeMs, screenHeight)
                respawnTimer -= deltaTimeMs
                if (respawnTimer <= 0L) {
                    if (lives > 0) {
                        respawn(screenWidth)
                    } else {
                        lifeState = PlayerLifeState.GAME_OVER
                    }
                }
            }
            PlayerLifeState.GAME_OVER -> {
                updateBullets(deltaTimeMs, screenHeight)
            }
        }
    }

    // The draw method makes the ship invisible when respawning/exploding.
    override fun draw(canvas: Canvas, paint: Paint) {
        // Only draw the sprite if the player is in a visible state.
        if (lifeState == PlayerLifeState.ALIVE || lifeState == PlayerLifeState.CAPTURED) {
            super.draw(canvas, paint)
        } else {
            // Still draw bullets even if the ship is invisible.
            bulletBank.drawAll(canvas, paint)
        }
    }

    // --- THIS IS THE NEW, CORRECT WAY TO CHANGE STATE ---
    fun changeLifeState(newState: PlayerLifeState) {
        if (lifeState == newState) return

        lifeState = newState
        // Update the visual appearance based on the new state's asset
        this.masterBitmap = assetLibrary.getBitmap(newState.asset)
        this.clearFrames()
        this.addFrameFromMaster(0, 0, masterBitmap.width, masterBitmap.height)
    }

    // Helper to give the HUD access to the queue for drawing.
    fun getPowerUpQueue(): List<PowerUpType> = powerUpQueue.toList()

    /**
     * Applies damage to the ship and signals if the damage was fatal.
     * @return `true` if the ship's health dropped to 0 or below, `false` otherwise.
     */
    fun takeDamage(amount: Int): Boolean {
        if (lifeState != PlayerLifeState.ALIVE) return false // Can't die if not alive

        hp -= amount
        if (hp <= 0) {
            lives--
            hp = 0
            changeLifeState(PlayerLifeState.EXPLODING)
            return true // Yes, a fatal hit occurred!
        }
        return false // No, the ship was only damaged.
    }

    // A new, simplified fire method that knows what type of bullet to use.
    fun fire() {
        currentWeapon.fire(this)
    }

    fun collectPowerUp(type: PowerUpType) {
        powerUpQueue.add(type)
    }

    private fun respawn(screenWidth: Float) {
        hp = maxHp
        changeLifeState(PlayerLifeState.ALIVE) // Respawning makes the player ALIVE again.
        this.x = (screenWidth / 2f) - (width / 2f)
        this.y = this.startY
        // TODO: Implement temporary invincibility
    }

    // This is called by the GameWorld on a double-tap.
    fun activateNextPowerUp() {
        // Can only activate a new power-up if one isn't already running.
        if (activePowerUp == null && powerUpQueue.isNotEmpty()) {
            val nextPowerUp = powerUpQueue.poll()!! // poll() retrieves and removes the head of the queue.
            activePowerUp = nextPowerUp
            powerUpTimerMs = nextPowerUp.durationMs

            // STRATEGY PATTERN: Swap the active weapon system.
            currentWeapon = when (nextPowerUp.grantsWeapon) {
                WeaponType.STANDARD_GUN -> StandardGun()
                WeaponType.PIERCING_GUN -> PiercingGun()
                // Add cases for Missiles and Laser here later
                else -> StandardGun()
            }
        }
    }
}