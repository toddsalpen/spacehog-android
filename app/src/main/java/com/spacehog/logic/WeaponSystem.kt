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
        // We'll need to make the base Ship.fire() method public or internal
        ship.fireWeapon(BulletType.PLAYER_STANDARD)
    }
    override fun update(deltaTimeMs: Long) { /* Does nothing */ }
}

// An implementation for the piercing gun.
class PiercingGun : WeaponSystem {
    override fun fire(ship: PlayerShip) {
        ship.fireWeapon(BulletType.PLAYER_PIERCING_SHOT)
    }
    override fun update(deltaTimeMs: Long) { /* Does nothing */ }
}

// In the future, MissileLauncher and LaserBeam would be their own classes here.
// The LaserBeam's update() method would be used to sustain the beam, for example.