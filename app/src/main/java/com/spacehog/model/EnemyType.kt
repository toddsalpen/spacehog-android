package com.spacehog.model

import androidx.annotation.DrawableRes
import com.spacehog.R
import com.spacehog.data.GameAsset

// This enum holds the unique "stats" for each kind of enemy.
enum class EnemyType(
    val asset: GameAsset, // Correct: It holds a reference to a GameAsset
    val hp: Int,
    val score: Int,
    val frameCount: Int,
    val virtualWidth: Float,
    val virtualHeight: Float
) {
    BLUE_BUG(
        asset = GameAsset.BLUE_BUG,
        hp = 1,
        score = 25,
        frameCount = 1, // The Blue Bug only has one frame
        virtualWidth = 120f, // A standard small, square-ish bug
        virtualHeight = 120f
    ),
    RED_BUG(
        asset = GameAsset.RED_BUG,
        hp = 1,
        score = 15,
        frameCount = 2, // The Red Bug has two frames
        virtualWidth = 120f, // A standard small, square-ish bug
        virtualHeight = 120f
    ),
    YELLOW_BUG(
        asset = GameAsset.YELLOW_BUG,
        hp = 1,
        score = 10,
        frameCount = 2, // Also has one frame
        virtualWidth = 140f, // Red bugs are a bit wider
        virtualHeight = 110f
    ),
    COMMANDER1(
        asset = GameAsset.COMMANDER1,
        hp = 2,
        score = 50,
        frameCount = 2, // For now, the Commander has one frame
        virtualWidth = 140f, // Red bugs are a bit wider
        virtualHeight = 110f
    ),
    COMMANDER2(
        asset = GameAsset.COMMANDER2,
        hp = 2,
        score = 50,
        frameCount = 2, // For now, the Commander has one frame
        virtualWidth = 140f, // Red bugs are a bit wider
        virtualHeight = 110f
    );
}