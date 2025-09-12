package com.spacehog.model

import kotlin.math.sqrt

/**
 * A modern, high-performance 2D vector class for Kotlin.
 *
 * This is a `data class` for automatic `equals`, `hashCode`, `copy`, and `toString` methods.
 * It is mutable (`var x`, `var y`) for high-performance use inside a game loop.
 *
 * It provides two ways to do math:
 * 1.  **Mutable Methods:** `add()`, `sub()`, etc., modify the vector in place and return `this` for chaining.
 * 2.  **Immutable Operators:** `+`, `-`, `*` create and return a *new* Vector2 instance, leaving the original unchanged.
 */
data class Vector2(var x: Float = 0f, var y: Float = 0f) {

    // --- Computed Properties (Always Correct, No Manual Updates) ---

    /** The Euclidean length (magnitude) of the vector. Calculated on-demand. */
    val magnitude: Float
        get() = sqrt(x * x + y * y)

    /** The squared length (magnitude) of the vector. Faster than `magnitude` for comparisons. */
    val magnitudeSq: Float
        get() = x * x + y * y

    // --- Mutable, Chaining Methods (For Performance) ---

    /** Sets the components of this vector. */
    fun set(x: Float, y: Float): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    /** Sets this vector's components from another vector. */
    fun set(other: Vector2): Vector2 {
        this.x = other.x
        this.y = other.y
        return this
    }

    /** Adds the given components to this vector. */
    fun add(dx: Float, dy: Float): Vector2 {
        this.x += dx
        this.y += dy
        return this
    }

    /** Subtracts the given components from this vector. */
    fun sub(dx: Float, dy: Float): Vector2 {
        this.x -= dx
        this.y -= dy
        return this
    }

    /** Multiplies this vector by a scalar. */
    fun multiply(scalar: Float): Vector2 {
        this.x *= scalar
        this.y *= scalar
        return this
    }

    /** Normalizes this vector to a length of 1. Does nothing if the vector is zero. */
    fun normalize(): Vector2 {
        val len = magnitude
        if (len != 0f) {
            this.x /= len
            this.y /= len
        }
        return this
    }

    // --- Immutable Operators (For Safety and Readability) ---

    operator fun plus(other: Vector2) = Vector2(this.x + other.x, this.y + other.y)
    operator fun minus(other: Vector2) = Vector2(this.x - other.x, this.y - other.y)
    operator fun times(scalar: Float) = Vector2(this.x * scalar, this.y * scalar)
    operator fun div(scalar: Float) = Vector2(this.x / scalar, this.y / scalar)

    // --- Utility Functions ---

    /** Calculates the dot product between this and another vector. */
    fun dot(other: Vector2): Float = this.x * other.x + this.y * other.y

    /** Calculates the distance between this and another vector. */
    fun distanceTo(other: Vector2): Float {
        val dx = other.x - this.x
        val dy = other.y - this.y
        return sqrt(dx * dx + dy * dy)
    }

    /** Calculates the squared distance between this and another vector. */
    fun distanceToSq(other: Vector2): Float {
        val dx = other.x - this.x
        val dy = other.y - this.y
        return dx * dx + dy * dy
    }
}