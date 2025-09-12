package com.spacehog.ui.game

import com.spacehog.model.Ship // We will create this next
import com.spacehog.model.Sprite // Other game objects

enum class GameStatus {
    PLAYING, PAUSED, GAME_OVER
}

data class GameState(
    val playerShip: Ship,
    val bullets: MutableList<Sprite> = mutableListOf(),
    val enemies: MutableList<Sprite> = mutableListOf(),
    var score: Int = 0,
    var status: GameStatus = GameStatus.PLAYING
)