package com.spacehog.model

enum class FireRate(val delayMs: Long) {
    TARDY(1000L),
    SLOW(500L),
    STANDARD(250L),
    FAST(125L),
    HYPER(75L),
    MACHINE_GUN(50L);

    val shotsPerSecond: Float
        get() = 1000f / delayMs

    // --- START OF THE NEW LOGIC ---
    companion object {
        /**
         * Calculates the required bullet pool size to prevent jamming.
         *
         * @param bulletLifetimeSeconds The longest possible time a bullet can be on screen.
         * @return The calculated pool size, including a small safety buffer.
         */
        fun calculateRequiredPoolSize(bulletLifetimeSeconds: Float): Int {
            // Find the fastest fire rate (the one with the minimum delay).
            val fastestRate = entries.minByOrNull { it.delayMs } ?: STANDARD

            // Calculate bullets needed and add a 20% buffer for safety.
            val requiredSize = bulletLifetimeSeconds * fastestRate.shotsPerSecond
            return (requiredSize * 1.2f).toInt() + 1
        }
    }
    // --- END OF THE NEW LOGIC ---
}