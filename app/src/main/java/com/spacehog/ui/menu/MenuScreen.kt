package com.spacehog.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spacehog.ui.common.StarfieldBackground
import com.spacehog.util.ImmersiveModeEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: MenuViewModel = viewModel() // <-- USE THE NEW VIEWMODEL
) {
    // Make this screen fullscreen
    ImmersiveModeEffect()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Solid black background
    ) {
        // Initialize the starfield with the screen dimensions
        LaunchedEffect(Unit) {
            viewModel.initializeStarfield(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        }

        // --- LAYER 1: The Background ---
        StarfieldBackground(stars = viewModel.stars)

        // --- LAYER 2: The UI ---
        Scaffold(
            containerColor = Color.Transparent, // See-through Scaffold
            topBar = {
                TopAppBar(
                    title = { Text("Space Hog") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // See-through App Bar
                        titleContentColor = Color.White // White text
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate("game") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Game")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings")
                }
            }
        }
    }
}