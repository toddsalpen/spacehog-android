package com.spacehog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spacehog.ui.game.GameScreen
import com.spacehog.ui.menu.MenuScreen
import com.spacehog.ui.splash.SplashScreen
import com.spacehog.ui.settings.SettingsScreen
import com.spacehog.util.GameFocusManager

@Composable
fun AppNavigation(
    gameFocusManager: GameFocusManager
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onFinish = {
                    navController.navigate("menu") {
                        popUpTo("splash") { inclusive = true }
                    }
            })
        }

        composable("menu") {
            // Your MenuScreen composable will go here
            // For now, we can navigate to settings as a placeholder
            MenuScreen(navController = navController)
        }

        // Add the Game route (for now, it can be a placeholder)
        composable("game") {
            GameScreen(navController = navController, gameFocusManager = gameFocusManager)
        }

        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}