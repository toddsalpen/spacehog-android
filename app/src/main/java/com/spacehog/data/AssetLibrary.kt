package com.spacehog.data // A good place for data/asset managers

import android.content.Context
import android.graphics.Bitmap

/**
 * A central library to pre-load and provide access to all game bitmaps.
 * This class is created once and holds all visual assets in memory for fast access.
 */
class AssetLibrary(context: Context) {
    // A map to hold the loaded bitmaps, keyed by our safe enum.
    private val bitmaps = mutableMapOf<GameAsset, Bitmap>()

    init {
        val drawables = DrawablesManager(context)
        // Pre-load all defined assets into memory.
        for (asset in GameAsset.entries) {
            bitmaps[asset] = drawables.getBitmap(asset.resourceId)
                ?: throw IllegalStateException("Failed to load required asset: ${asset.name}")
        }
    }

    /**
     * Safely retrieves a pre-loaded bitmap from the library.
     */
    fun getBitmap(asset: GameAsset): Bitmap {
        return bitmaps[asset]!! // We can use !! because the init block guarantees they all exist.
    }
}