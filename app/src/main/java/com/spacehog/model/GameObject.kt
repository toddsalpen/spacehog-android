package com.spacehog.model

import android.graphics.RectF
import kotlin.math.pow

/**
 * A base class for any object in the game world that has a position, dimensions,
 * and can be involved in collisions.
 *
 * It uses Kotlin properties and provides corrected, efficient collision detection methods.
 */
open class GameObject(
    var width: Float,
    var height: Float,
    var x: Float,
    var y: Float
) {

    /**
     * A computed property that calculates the object's bounding box on the fly.
     *
     * BEST PRACTICE: This prevents the 'rect' from ever being out of sync with the
     * object's x, y, width, or height, which was a major bug in the original Java version.
     * It is always up-to-date.
     */
    val rect: RectF
        get() = RectF(x, y, x + width, y + height)

    /**
     * A convenience function to update the object's position.
     */
    fun setPosition(newX: Float, newY: Float) {
        this.x = newX
        this.y = newY
    }

    // --- COLLISION DETECTION ---

    /**
     * Checks for collision with another object, assuming both are circles.
     * NOTE: This assumes the object's radius is half its width.
     */
    fun circleCollision(other: GameObject): Boolean {
        // CORRECTED LOGIC: Collision is checked based on the distance between object centers.
        val centerX1 = this.x + this.width / 2f
        val centerY1 = this.y + this.height / 2f
        val centerX2 = other.x + other.width / 2f
        val centerY2 = other.y + other.height / 2f

        val distanceSq = (centerX1 - centerX2).pow(2) + (centerY1 - centerY2).pow(2)
        val combinedRadiiSq = (this.width / 2f + other.width / 2f).pow(2)

        return distanceSq < combinedRadiiSq
    }

    /**
     * Checks for Axis-Aligned Bounding Box (AABB) collision with another GameObject.
     */
    fun rectCollision(other: GameObject): Boolean {
        // CORRECTED LOGIC: Standard AABB intersection test.
        return this.x < other.x + other.width &&
                this.x + this.width > other.x &&
                this.y < other.y + other.height &&
                this.y + this.height > other.y
    }

    /**
     * Checks for intersection with a given RectF without modifying either rectangle.
     */
    fun rectCollision(other: RectF): Boolean {
        // CORRECTED LOGIC: Use the non-mutating `intersects` static method.
        // The original `rect.intersect(other)` was a bug that modified the object's rect.
        return RectF.intersects(this.rect, other)
    }

    /**
     * Checks for collision between this object (treated as a circle) and another (treated as a rect).
     */
    fun circleRectCollision(ball: GameObject, block: GameObject): Boolean {
        // DRY Principle: Delegate to the private helper function.
        return checkCircleRectCollision(
            circleX = ball.x + ball.width / 2f,
            circleY = ball.y + ball.height / 2f,
            circleRadius = ball.width / 2f,
            rect = block.rect
        )
    }

    /**
     * Overload for checking collision between a circle object and a raw RectF.
     */
    fun circleRectCollision(ball: GameObject, block: RectF): Boolean {
        // DRY Principle: Delegate to the private helper function.
        return checkCircleRectCollision(
            circleX = ball.x + ball.width / 2f,
            circleY = ball.y + ball.height / 2f,
            circleRadius = ball.width / 2f,
            rect = block
        )
    }

    /**
     * Private helper containing the core logic for circle-rectangle collision detection.
     * This avoids code duplication.
     */
    private fun checkCircleRectCollision(circleX: Float, circleY: Float, circleRadius: Float, rect: RectF): Boolean {
        // Find the closest point on the rectangle's edge to the circle's center
        val closestX = circleX.coerceIn(rect.left, rect.right)
        val closestY = circleY.coerceIn(rect.top, rect.bottom)

        // Calculate the distance between the circle's center and this closest point
        val distanceX = circleX - closestX
        val distanceY = circleY - closestY

        // If the distance is less than the circle's radius, there's a collision
        val distanceSquared = (distanceX * distanceX) + (distanceY * distanceY)
        return distanceSquared < (circleRadius * circleRadius)
    }
}