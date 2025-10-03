package com.spacehog.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacehog.logic.StarfieldManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class StarfieldViewModel : ViewModel() {
    // --- THE REFACTOR ---
    // The ViewModel now OWNS the single source of truth for starfield logic.
    private val starfieldManager = StarfieldManager()

    // It exposes the star state to the Compose UI.
    var stars by mutableStateOf<List<StarState>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            while (true) {
                // It calls the manager's update method.
                starfieldManager.update()
                // It then updates its own public state to trigger recomposition in the UI.
                stars = starfieldManager.starStates
                delay(16)
            }
        }
    }

    // The initialize method just passes the command along to the manager.
    fun initializeStarfield(width: Float, height: Float) {
        starfieldManager.initialize(width, height)
    }

    // The private updateStarfield method is now GONE. All logic is in the manager.
}