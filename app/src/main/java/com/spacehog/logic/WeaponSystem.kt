package com.spacehog.logic

import com.spacehog.model.BulletType
import com.spacehog.model.PlayerShip

// An interface defining what any weapon system MUST be able to do.
interface WeaponSystem {
    fun fire(ship: PlayerShip)
    fun update(deltaTimeMs: Long)
}

// An implementation for the standard gun.
class StandardGun : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        if (ship.canFire()) {
            ship.fireProjectile(BulletType.PLAYER_STANDARD, ship.x + ship.width / 2f, ship.y)
            ship.resetFireCooldown()
        }
    }
    override fun update(deltaTimeMs: Long) { /* Does nothing */ }
}

// An implementation for the piercing gun.
class PiercingGun : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        if (ship.canFire()) {
            ship.fireProjectile(BulletType.PLAYER_PIERCING_SHOT, ship.x + ship.width / 2f, ship.y)
            ship.resetFireCooldown()
        }
    }
    override fun update(deltaTimeMs: Long) { /* Does nothing */ }
}

class DoubleGun : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        // 1. Check the cooldown first.
        if (ship.canFire()) {
            val gunOffset = ship.width / 4f
            val leftGunX = ship.x + gunOffset
            val rightGunX = ship.x + (ship.width - gunOffset)
            val fireY = ship.y

            // 2. Fire the projectiles.
            ship.fireProjectile(BulletType.PLAYER_STANDARD, leftGunX, fireY)
            ship.fireProjectile(BulletType.PLAYER_STANDARD, rightGunX, fireY)

            // 3. Reset the cooldown once for the entire volley.
            ship.resetFireCooldown()
        }
    }
    override fun update(deltaTimeMs: Long) {}
}

// In the future, MissileLauncher and LaserBeam would be their own classes here.
// The LaserBeam's update() method would be used to sustain the beam, for example.