package com.spacehog.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spacehog.R // Import your R file

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    // State to control the visibility of our dialogs
    var showResetDialog by remember { mutableStateOf(false) }
    var showControlsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Button to open the controls dialog
            Button(
                onClick = { showControlsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Select Control Style")
            }

            // Button to open the reset scores dialog
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Reset High Scores")
            }
        }
    }

    // --- DIALOGS ---

    // The confirmation dialog for resetting scores
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Are You Sure?") },
            text = { Text(text = "This will permanently delete the scoreboard and cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onResetScores(context)
                        showResetDialog = false
                    }
                ) {
                    Text("DO IT!")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Never Mind")
                }
            }
        )
    }

    // The dialog for choosing control styles
    if (showControlsDialog) {
        val controlOptions = listOf(
            "Standard - Touch To Move and Fire",
            "Tilt - Tilt To Move and Touch to Fire"
        )
        AlertDialog(
            onDismissRequest = { showControlsDialog = false },
            title = { Text("Please Select A Control Style") },
            text = {
                Column {
                    controlOptions.forEachIndexed { index, text ->
                        TextButton(
                            onClick = {
                                viewModel.onControlStyleSelected(context, index)
                                //showControlsoldsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text)
                        }
                    }
                }
            },
            confirmButton = {} // No confirm button needed as selection is instant
        )
    }
}