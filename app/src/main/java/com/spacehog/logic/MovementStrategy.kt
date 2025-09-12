package com.spacehog.logic

import com.spacehog.model.Enemy
import com.spacehog.model.Vector2
import kotlin.random.Random

// This enum gives us a safe, readable way to refer to our movement patterns in the level data.
enum class MovementPatternType {
    STRAIGHT_DOWN,
    ZIG_ZAG,
    DIAGONAL,
    HUNTER
}

// This is the "contract" that every movement strategy must follow.
// It must be able to update an enemy's position.
interface MovementStrategy {
    fun update(enemy: Enemy, deltaTimeMs: Long, screenHeight: Float, screenWidth: Float)
    fun onOutOfBounds(enemy: Enemy) {}
}

class StraightDownStrategy: MovementStrategy {
    override fun update(enemy: Enemy, deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        val normalizedDelta = deltaTimeMs / 16f
        enemy.y += enemy.movementSpeed * normalizedDelta
    }
}

class ZigZagStrategy(private val frequency: Float = 0.05f) : MovementStrategy {
    private val timers = java.util.WeakHashMap<Enemy, Float>()
    override fun update(enemy: Enemy, deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        val normalizedDelta = deltaTimeMs / 16f
        enemy.y += enemy.movementSpeed * normalizedDelta
        val timer = timers.getOrPut(enemy) { 0f }
        enemy.x += kotlin.math.sin(timer) * enemy.movementSpeed
        timers[enemy] = timer + (frequency * normalizedDelta)
    }
}

/** Moves from one corner to the opposite corner and wraps. */
class DiagonalStrategy : MovementStrategy {
    private val vectors = java.util.WeakHashMap<Enemy, Vector2>()

    override fun update(enemy: Enemy, deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        val normalizedDelta = deltaTimeMs / 16f
        val vector = vectors.getOrPut(enemy) {
            // Generation logic is the same...
            val startsOnLeft = kotlin.random.Random.nextBoolean()
            enemy.x = if (startsOnLeft) -enemy.width else screenWidth
            enemy.y = kotlin.random.Random.nextFloat() * (screenHeight / 2)
            val targetX = if (startsOnLeft) screenWidth else -enemy.width
            Vector2(targetX - enemy.x, screenHeight - enemy.y).normalize()
        }
        enemy.x += vector.x * enemy.movementSpeed * normalizedDelta
        enemy.y += vector.y * enemy.movementSpeed * normalizedDelta
    }

    // We override this to reset the vector, forcing a new path on wrap
    override fun onOutOfBounds(enemy: Enemy) {
        vectors.remove(enemy)
    }
}

/** A more advanced strategy that combines other behaviors. */
class HunterStrategy(private val reverseSpeed: Float = -6f) : MovementStrategy {
    private enum class State { FORWARD, REVERSING }
    private val states = java.util.WeakHashMap<Enemy, State>()
    private val timers = java.util.WeakHashMap<Enemy, Long>()

    override fun update(enemy: Enemy, deltaTimeMs: Long, screenHeight: Float, screenWidth: Float) {
        val normalizedDelta = deltaTimeMs / 16f
        val currentState = states.getOrPut(enemy) { State.FORWARD }

        when (currentState) {
            State.FORWARD -> {
                enemy.y += enemy.movementSpeed * normalizedDelta
                if (enemy.y > screenHeight * 0.7f && kotlin.random.Random.nextInt(100) > 95) {
                    states[enemy] = State.REVERSING
                    timers[enemy] = 2000L
                }
            }
            State.REVERSING -> {
                enemy.y += reverseSpeed * normalizedDelta
                val timer = timers.getOrDefault(enemy, 0L) - deltaTimeMs
                timers[enemy] = timer
                if (timer <= 0L) {
                    states[enemy] = State.FORWARD
                }
            }
        }
    }

    // Reset the state to FORWARD when wrapping
    override fun onOutOfBounds(enemy: Enemy) {
        states[enemy] = State.FORWARD
    }
}