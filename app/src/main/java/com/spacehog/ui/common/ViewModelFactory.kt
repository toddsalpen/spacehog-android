package com.spacehog.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spacehog.logic.StarfieldManager
import com.spacehog.ui.menu.MenuViewModel
import com.spacehog.ui.settings.SettingsViewModel

/**
 * A custom factory for creating ViewModels that require a StarfieldManager dependency.
 * This is the core of our dependency injection for the UI layer.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val starfieldManager: StarfieldManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // This 'when' block makes the factory smart.
        // It checks what kind of ViewModel is being requested...
        return when {
            // ...if it's a MenuViewModel, create one with the starfieldManager.
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> {
                MenuViewModel(starfieldManager) as T
            }
            // ...if it's a SettingsViewModel, create one with the starfieldManager.
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(starfieldManager) as T
            }
            // If we ever add more ViewModels that need the manager, we just add another line here.

            // Otherwise, we don't know how to create it, so we throw an error.
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}