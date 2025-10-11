package com.spacehog.data

import com.spacehog.logic.MovementPatternType
import com.spacehog.model.EnemyType
import com.spacehog.model.FireRate
import com.spacehog.model.MovementSpeed

data class LevelProperties(
    val playerFireRate: FireRate,
    val enemyFireRate: FireRate,
    val spawnEvents: List<SpawnEvent>
)

// In a new file, e.g., LevelData.kt
data class SpawnEvent(
    val timeMs: Long,      // At what time in the level should this happen?
    val enemyType: EnemyType,
    val startX: Float,
    val startY: Float = -100f,
    val movementPattern: MovementPatternType,
    val movementSpeed: MovementSpeed // Default Y to be off-screen at the top
)

// This object can hold the data for all our levels
object Levels {
    val LEVEL_1 = LevelProperties(
        playerFireRate = FireRate.MACHINE_GUN,
        enemyFireRate = FireRate.SLOW,
        spawnEvents = listOf(
            // A simple wave of Blue Bugs
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 100f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(1500L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(2000L, EnemyType.BLUE_BUG, 900f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.NORMAL),
            // Introduce the first Red Bug
            SpawnEvent(3500L, EnemyType.RED_BUG, 500f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.FAST),
            SpawnEvent(4000L, EnemyType.RED_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(4000L, EnemyType.RED_BUG, 700f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
        )
    )

    val LEVEL_2 = LevelProperties(
            playerFireRate = FireRate.MACHINE_GUN,
            enemyFireRate = FireRate.STANDARD,
            spawnEvents = listOf(
                // Faster, more dense waves
                SpawnEvent(1000L, EnemyType.RED_BUG, 200f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.SLOW),
                SpawnEvent(1000L, EnemyType.RED_BUG, 800f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.NORMAL),
                SpawnEvent(1500L, EnemyType.YELLOW_BUG, 500f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.FAST),

                // A "V" formation
                SpawnEvent(3000L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.VFAST),
                SpawnEvent(3250L, EnemyType.BLUE_BUG, 400f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
                SpawnEvent(3250L, EnemyType.BLUE_BUG, 600f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.SLOW),
                SpawnEvent(3500L, EnemyType.BLUE_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.NORMAL),
                SpawnEvent(3500L, EnemyType.BLUE_BUG, 700f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.FAST),

                // Introduce the first Commander
                SpawnEvent(5000L, EnemyType.COMMANDER1, 500f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.VFAST),
            )
    )

    val LEVEL_3 = LevelProperties(
        playerFireRate = FireRate.MACHINE_GUN,
        enemyFireRate = FireRate.SLOW,
        spawnEvents = listOf(
            // A simple wave of Blue Bugs
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 100f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(1500L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(2000L, EnemyType.BLUE_BUG, 900f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.NORMAL),
            // Introduce the first Red Bug
            SpawnEvent(3500L, EnemyType.RED_BUG, 500f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.FAST),
            SpawnEvent(4000L, EnemyType.RED_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(4000L, EnemyType.RED_BUG, 700f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
        )
    )


    val LEVEL_4 = LevelProperties(
        playerFireRate = FireRate.MACHINE_GUN,
        enemyFireRate = FireRate.STANDARD,
        spawnEvents = listOf(
            // Faster, more dense waves
            SpawnEvent(1000L, EnemyType.RED_BUG, 200f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.SLOW),
            SpawnEvent(1000L, EnemyType.RED_BUG, 800f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(1500L, EnemyType.YELLOW_BUG, 500f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.FAST),

            // A "V" formation
            SpawnEvent(3000L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.VFAST),
            SpawnEvent(3250L, EnemyType.BLUE_BUG, 400f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(3250L, EnemyType.BLUE_BUG, 600f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.SLOW),
            SpawnEvent(3500L, EnemyType.BLUE_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(3500L, EnemyType.BLUE_BUG, 700f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.FAST),

            // Introduce the first Commander
            SpawnEvent(5000L, EnemyType.COMMANDER1, 500f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.VFAST),
        )
    )
    val LEVEL_5 = LevelProperties(
        playerFireRate = FireRate.MACHINE_GUN,
        enemyFireRate = FireRate.SLOW,
        spawnEvents = listOf(
            // A simple wave of Blue Bugs
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 100f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(1500L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(2000L, EnemyType.BLUE_BUG, 900f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.NORMAL),
            // Introduce the first Red Bug
            SpawnEvent(3500L, EnemyType.RED_BUG, 500f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.FAST),
            SpawnEvent(4000L, EnemyType.RED_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(4000L, EnemyType.RED_BUG, 700f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.NORMAL),
        )
    )

    // In LevelData.kt, inside the Levels object
    val LEVEL_10_HORDE = LevelProperties(
        playerFireRate = FireRate.MACHINE_GUN, // The player doesn't get a boost
        enemyFireRate = FireRate.STANDARD,      // Set a FAST base rate for all enemies on this level

        spawnEvents = listOf(
            // Wave 1: A wall of Blue Bugs
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 100f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 300f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.VFAST),
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 700f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.VFAST),
            SpawnEvent(1000L, EnemyType.BLUE_BUG, 900f, movementPattern = MovementPatternType.DIAGONAL, movementSpeed = MovementSpeed.TARDY),

            // Wave 2: Fast-moving Yellow flankers
            SpawnEvent(3000L, EnemyType.YELLOW_BUG, 50f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(3200L, EnemyType.YELLOW_BUG, 1000f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.SLOW),
            SpawnEvent(3400L, EnemyType.YELLOW_BUG, 50f, movementPattern = MovementPatternType.HUNTER, movementSpeed = MovementSpeed.NORMAL),
            SpawnEvent(3600L, EnemyType.YELLOW_BUG, 1000f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.FAST),

            // Wave 3: Heavy Red Bugs protecting a Commander
            SpawnEvent(6000L, EnemyType.RED_BUG, 300f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.VFAST),
            SpawnEvent(6000L, EnemyType.RED_BUG, 700f, movementPattern = MovementPatternType.ZIG_ZAG, movementSpeed = MovementSpeed.TARDY),
            SpawnEvent(6500L, EnemyType.COMMANDER2, 500f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.SLOW),
            SpawnEvent(6500L, EnemyType.COMMANDER1, 900f, movementPattern = MovementPatternType.STRAIGHT_DOWN, movementSpeed = MovementSpeed.SLOW), // Mini-boss appears
        )
    )
}