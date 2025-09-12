package com.spacehog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spacehog.ui.game.GameScreen
import com.spacehog.ui.menu.MenuScreen
import com.spacehog.ui.splash.SplashScreen
import com.spacehog.ui.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") { // Start at the splash screen

        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    // When the splash is done, navigate to menu and remove
                    // the splash screen from the back stack.
                    navController.navigate("menu") { // Assuming "menu" is your next screen
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("menu") {
            // Your MenuScreen composable will go here
            // For now, we can navigate to settings as a placeholder
            MenuScreen(navController = navController)
        }


        // Add the Game route (for now, it can be a placeholder)
        composable("game") {
            GameScreen(navController = navController)
        }

        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}