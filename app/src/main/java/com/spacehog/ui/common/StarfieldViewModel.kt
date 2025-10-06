package com.spacehog.ui.common

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spacehog.logic.StarfieldManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class StarfieldViewModel(
    private val starfieldManager: StarfieldManager // <-- RECEIVE it
) : ViewModel() {
    var stars by mutableStateOf<List<StarState>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            while (true) {
                starfieldManager.update()
                stars = starfieldManager.getStarStates()
                delay(16)
            }
        }
    }
}