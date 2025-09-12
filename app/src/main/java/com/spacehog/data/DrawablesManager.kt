package com.spacehog.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

/**
 * Manages loading and caching of Bitmap resources in a memory-efficient way.
 *
 * This class implements a lazy-loading strategy. A Bitmap is only decoded from resources
 * the first time it's requested by its name. Subsequent requests for the same Bitmap
 * will return a cached instance, avoiding redundant decoding.
 */
class DrawablesManager(private val context: Context) {

    // The cache holds bitmaps that have already been loaded.
    // The key is the drawable's string name (e.g., "space_hog").
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    /**
     * Retrieves a Bitmap by its resource name (e.g., "space_hog").
     *
     * If the bitmap is already in the cache, it's returned immediately.
     * If not, it's loaded from drawable resources, added to the cache, and then returned.
     *
     * @param resourceId The file name of the drawable without the extension.
     * @return The requested [Bitmap], or null if the drawable could not be found or decoded.
     */
    fun getBitmap(resourceId: Int): Bitmap? {
        // Return from cache if it exists, otherwise execute the 'run' block to load it.
        return bitmapCache[resourceId] ?: run {
            try {
                BitmapFactory.decodeResource(context.resources, resourceId)?.also {
                    bitmapCache[resourceId] = it
                }
            } catch (e: Exception) {
                Log.e("DrawablesManager", "Failed to decode bitmap for resource ID: $resourceId", e)
                null
            }
        }
    }

    /**
     * Clears the entire bitmap cache.
     *
     * This can be called when you know the cached bitmaps are no longer needed
     * to free up memory (e.g., when leaving a game screen).
     */
    fun clearCache() {
        Log.d("DrawablesManager", "Clearing bitmap cache.")
        bitmapCache.clear()
    }
}