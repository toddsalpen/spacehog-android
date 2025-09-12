package com.spacehog.logic

import com.spacehog.model.LevelState

// A simple data container for all our debugging values.
data class DebugData(
    val levelNumber: Int = 0,
    val levelState: LevelState = LevelState.RUNNING,
    val totalEnemiesSpawned: Int = 0,
    val enemiesDefeatedInLevel: Int = 0,
    val activeEnemiesOnScreen: Int = 0,
    val activePlayerBullets: Int = 0,
    val playerHP: Int = 0,
    val playerLives: Int = 0
    // We can add more variables to track here in the future
)