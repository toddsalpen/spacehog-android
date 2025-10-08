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
import com.spacehog.logic.StarfieldManager
import com.spacehog.ui.common.StarfieldBackground
import com.spacehog.ui.common.ViewModelFactory
import com.spacehog.util.ImmersiveModeEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController: NavController,
    starfieldManager: StarfieldManager // <-- USE THE NEW VIEWMODEL
) {

    val factory = ViewModelFactory(starfieldManager)
    val viewModel: MenuViewModel = viewModel(factory = factory)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Solid black background
    ) {
        StarfieldBackground(stars = viewModel.stars)
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