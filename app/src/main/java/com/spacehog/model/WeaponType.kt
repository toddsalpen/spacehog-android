package com.spacehog.model

import com.spacehog.data.GameAsset

// Defines the different types of weapons the player can have.
enum class WeaponType {
    STANDARD_GUN,
    PIERCING_GUN,
    MISSILE_LAUNCHER,
    LASER_BEAM
}

// Defines the properties of the collectible power-up items.
enum class PowerUpType(
    val grantsWeapon: WeaponType, // What weapon does this power-up give?
    val durationMs: Long,         // How long does it last?
    val itemAsset: GameAsset,       // What does the floating item look like?
    val hudAsset: GameAsset         // What does the icon in the player's queue look like?
) {
    PIERCING_SHOT(
        grantsWeapon = WeaponType.PIERCING_GUN,
        durationMs = 10_000L, // 10 seconds
        itemAsset = GameAsset.POWERUP_ITEM_PIERCING, // New assets needed
        hudAsset = GameAsset.HUD_ICON_PIERCING
    ),
    MISSILES(
        grantsWeapon = WeaponType.MISSILE_LAUNCHER,
        durationMs = 15_000L, // 15 seconds
        itemAsset = GameAsset.POWERUP_ITEM_MISSILE,
        hudAsset = GameAsset.HUD_ICON_MISSILE
    );
    // You can easily add the Laser here later.
}