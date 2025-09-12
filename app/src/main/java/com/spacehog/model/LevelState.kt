package com.spacehog.model

// This enum will represent the current state of the level progression.
enum class LevelState {
    RUNNING,      // The level is currently spawning enemies.
    INTERMISSION, // The level is complete, waiting to start the next.
    CAMPAIGN_COMPLETE // All levels are finished.
}
