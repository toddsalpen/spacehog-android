package com.spacehog.model

import com.spacehog.data.GameAsset

enum class MovementSpeed(val pixelsPerFrame: Float) {
    TARDY(0.5f),
    SLOW(2.5f),
    NORMAL(5.0f),
    FAST(7.5f),
    VFAST(20.0f)
}

// Defines the visual state of the player's ship
enum class PlayerState(val asset: GameAsset) {
    NORMAL(GameAsset.PLAYER_SHIP),
    CAPTURED(GameAsset.CAPTURED_SHIP)
}

// Defines the different types of bullets
enum class BulletType(
    val asset: GameAsset,
    val speed: Float,
    val damage: Int,
    val pierces: Boolean // <-- NEW PROPERTY
) {
    PLAYER_STANDARD(
        asset = GameAsset.PLAYER_BULLET,
        speed = -25f,
        damage = 1,
        pierces = false // Standard bullets do not pierce
    ),
    ENEMY_STANDARD(
        asset = GameAsset.ENEMY_BULLET,
        speed = 15f,
        damage = 1,
        pierces = false
    ),
    // A future power-up!
    PLAYER_PIERCING_SHOT(
        asset = GameAsset.PLAYER_BULLET, // Can reuse the same visual for now
        speed = -35f,
        damage = 2,
        pierces = true // This bullet will keep going!
    );
}