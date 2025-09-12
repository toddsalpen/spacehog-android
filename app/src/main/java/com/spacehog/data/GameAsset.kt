package com.spacehog.data

import androidx.annotation.DrawableRes
import com.spacehog.R

// This enum defines ALL the drawable assets our game uses.
// It's the single source of truth for assets. It's typo-proof!
enum class GameAsset(@param:DrawableRes val resourceId: Int) {
    // Player Sprites
    PLAYER_SHIP(R.drawable.player_ship),
    CAPTURED_SHIP(R.drawable.captured_ship), // You'll need to add this png

    // Enemy Sprites
    BLUE_BUG(R.drawable.blue_bug),
    RED_BUG(R.drawable.red_bug),
    YELLOW_BUG(R.drawable.yellow_bug),
    COMMANDER1(R.drawable.commander1),
    COMMANDER2(R.drawable.commander2),

    // Bullet Sprites
    PLAYER_BULLET(R.drawable.player_bullet), // Add player_bullet.png
    ENEMY_BULLET(R.drawable.enemy_bullet),  // Add enemy_bullet.png

    // Power Ups
    POWERUP_ITEM_PIERCING(R.drawable.powerup_item_piercing),
    HUD_ICON_PIERCING(R.drawable.hud_icon_piercing),
    POWERUP_ITEM_MISSILE(R.drawable.powerup_item_missile),
    HUD_ICON_MISSILE(R.drawable.hud_icon_missile),
    POWERUP_ITEM_LASER(R.drawable.powerup_item_laser),
    HUD_ICON_LASER(R.drawable.hud_icon_laser),

    // In the future, you can add explosions, power-ups, etc. here
    // EXPLOSION(R.drawable.explosion),
    EXPLOSION_ENEMY(R.drawable.explosion_enemy),
    EXPLOSION_PLAYER(R.drawable.explosion_player);
}