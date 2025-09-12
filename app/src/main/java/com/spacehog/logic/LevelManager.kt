package com.spacehog.logic

import com.spacehog.data.LevelProperties
import com.spacehog.data.Levels
import com.spacehog.data.SpawnEvent
import com.spacehog.model.LevelState
import java.util.LinkedList
import java.util.Queue

class LevelManager(private val enemyManager: EnemyManager) {

    private val campaign: Queue<LevelProperties> = LinkedList()
    var currentLevelNumber = 0
        private set
    // This now holds all the rules for the current level
    var currentLevelProperties: LevelProperties? = null
    var state: LevelState = LevelState.RUNNING
        private set

    private val intermissionTimeMs = 3000L // 3-second delay between levels
    private var intermissionTimer = 0L

    private var levelTimer = 0L
    private var spawnQueue: Queue<SpawnEvent> = LinkedList() // Use a queue for efficiency

    var totalEnemiesSpawned = 0
    var enemiesDefeatedInLevel = 0

    init {
        // --- CAMPAIGN SETUP ---
        // Load all levels into our campaign queue
        campaign.add(Levels.LEVEL_1)
        campaign.add(Levels.LEVEL_2)
        campaign.add(Levels.LEVEL_10_HORDE)
        // You can add Levels.LEVEL_3, etc., here later.

        // Start the first level
        startNextLevel()
    }

    // GameWorld will call this method whenever an enemy is defeated.
    fun onEnemyDefeated() {
        enemiesDefeatedInLevel++
    }

    fun update(deltaTimeMs: Long) {
        // Use a state machine to control the level manager's behavior
        when (state) {
            LevelState.RUNNING -> {
                levelTimer += deltaTimeMs

                // Spawn enemies based on the timer
                while (spawnQueue.isNotEmpty() && spawnQueue.peek()!!.timeMs <= levelTimer) {
                    val event = spawnQueue.poll()!!
                    enemyManager.spawnEnemy(
                        event.enemyType,
                        event.startX,
                        event.startY,
                        currentLevelProperties!!.enemyFireRate,
                        event.movementPattern,
                        event.movementSpeed
                    )
                }

                // Check if the level is complete
//                if (spawnQueue.isEmpty() && enemyManager.getActiveEnemies().isEmpty()) {
//                    // All spawned enemies are now destroyed. Start the intermission.
//                    state = LevelState.INTERMISSION
//                    intermissionTimer = intermissionTimeMs
//                }

                if (enemiesDefeatedInLevel >= totalEnemiesSpawned) {
                    // All spawned enemies for this level have been defeated.
                    state = LevelState.INTERMISSION
                    intermissionTimer = intermissionTimeMs
                }
            }
            LevelState.INTERMISSION -> {
                // Countdown the timer between levels
                intermissionTimer -= deltaTimeMs
                if (intermissionTimer <= 0) {
                    // Timer is up, start the next level
                    startNextLevel()
                }
            }
            LevelState.CAMPAIGN_COMPLETE -> {
                // Do nothing. The game is won.
            }
        }
    }

    private fun startNextLevel() {
        if (campaign.isNotEmpty()) {
            // Get the next level from the campaign queue
            val nextLevel = campaign.poll()!!
            currentLevelProperties = nextLevel // Store the current level's properties
            spawnQueue = LinkedList(nextLevel.spawnEvents) // Get the spawn events from the properties

            // Reset the counters for the new level
            totalEnemiesSpawned = nextLevel.spawnEvents.size
            enemiesDefeatedInLevel = 0

            currentLevelNumber++
            levelTimer = 0L
            state = LevelState.RUNNING
        } else {
            // No more levels left to play
            state = LevelState.CAMPAIGN_COMPLETE
        }
    }

    fun isFinished(): Boolean {
        return spawnQueue.isEmpty() && enemyManager.getActiveEnemies().isEmpty()
    }
}

