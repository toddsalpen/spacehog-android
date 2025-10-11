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
            val fireY = ship.y
            ship.fireProjectile(BulletType.PLAYER_STANDARD, ship.x + ship.width / 2f, fireY)
            ship.resetFireCooldown()
        }
    }
    override fun update(deltaTimeMs: Long) { /* Does nothing */ }
}

// An implementation for the piercing gun.
class PiercingGun : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        if (ship.canFire()) {
            val fireY = ship.y
            ship.fireProjectile(BulletType.PLAYER_PIERCING_SHOT, ship.x + ship.width / 2f, fireY)
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

class MissileLauncher : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        // 1. Check if the ship's weapon is ready (respects the fireRate cooldown).
        if (ship.canFire()) {
            val fireX = ship.x + ship.width / 2f
            val fireY = ship.y - 10f
            // 2. Fire one projectile of the PLAYER_HOMING_MISSILE type from the center of the ship.
            ship.fireProjectile(
                bulletType = BulletType.PLAYER_HOMING_MISSILE,
                fireX = fireX,
                fireY = fireY
            )

            // 3. Reset the cooldown once the missile is launched.
            ship.resetFireCooldown()
        }
    }

    override fun update(deltaTimeMs: Long) {
        // Missiles don't have special update logic at the system level, so this is empty.
    }
}

// In the future, MissileLauncher and LaserBeam would be their own classes here.
// The LaserBeam's update() method would be used to sustain the beam, for example.