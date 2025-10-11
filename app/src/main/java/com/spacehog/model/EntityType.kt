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
    val asset: GameAsset,    // What visual asset does this bullet use?
    val speed: Float,        // How fast does this bullet move?
    val damage: Int,         // How much damage does this bullet do?
    val pierces: Boolean,    // Does this bullet pierce enemies?
    val isHoming: Boolean,   // Does this bullet track targets?
    val turnRate: Float      // How quickly can it turn? (degrees per update)
) {
    PLAYER_STANDARD(
        asset = GameAsset.PLAYER_BULLET,
        speed = -25f,
        damage = 1,
        pierces = false,
        isHoming = false, // Standard bullets don't home in
        turnRate = 0f
    ),
    ENEMY_STANDARD(
        asset = GameAsset.ENEMY_BULLET,
        speed = 15f,
        damage = 1,
        pierces = false,
        isHoming = false, // Standard bullets don't home in
        turnRate = 0f
    ),
    // A future power-up!
    PLAYER_PIERCING_SHOT(
        asset = GameAsset.PLAYER_BULLET, // Can reuse the same visual for now
        speed = -35f,
        damage = 2,
        pierces = true,
        isHoming = false, // Standard bullets don't home in
        turnRate = 0f
    ),
    PLAYER_HOMING_MISSILE(
        asset = GameAsset.PLAYER_MISSILE, // Use a new missile sprite
        speed = 20f, // Homing missiles can have a constant forward speed
        damage = 3,
        pierces = false,
        isHoming = true, // This is a homing missile!
        turnRate = 5f    // It can turn 5 degrees per update cycle
    );
}