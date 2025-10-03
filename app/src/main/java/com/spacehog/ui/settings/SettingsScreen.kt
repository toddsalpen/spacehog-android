package com.spacehog.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spacehog.ui.common.StarfieldBackground
import com.spacehog.util.ImmersiveModeEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    // Make this screen fullscreen
    ImmersiveModeEffect()
    val context = LocalContext.current
    val isFocusEnabled by remember { derivedStateOf { viewModel.isFocusModeEnabled(context) } }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* After returning, state will automatically update */ }

    // This Box allows us to layer our background and UI.
    // BoxWithConstraints gives us the screen dimensions to initialize the starfield.
    BoxWithConstraints(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {

        // This effect will run once when the screen dimensions are first known.
        LaunchedEffect(Unit) {
            viewModel.initializeStarfield(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        }

        // --- LAYER 1: The Background ---
        StarfieldBackground(stars = viewModel.stars)

        // --- LAYER 2: The UI ---
        Scaffold(
            // Make the Scaffold transparent so we can see the starfield behind it
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(text = "Settings") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    // Also make the app bar transparent
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // App bar is also see-through
                        titleContentColor = Color.White, // Force title to be white
                        navigationIconContentColor = Color.White // Force icon to be white
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // ... Your settings items go here. Let's re-use the helpers ...
                var showControlsDialog by remember { mutableStateOf(false) }
                var showResetDialog by remember { mutableStateOf(false) }

                // Control Style Setting
                SettingsItem(
                    title = "Control Style",
                    summary = "Select your preferred control style"
                ){  showControlsDialog = true }

                // Reset Scores Setting
                SettingsItem(
                    title = "Reset High Scores",
                    summary = "Permanently delete the scoreboard"
                ) { showResetDialog = true }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = Color.White.copy(alpha=0.5f)
                )

                // Focus Mode Switch
                SettingsSwitchItem(
                    title = "Game Focus Mode",
                    summary = "Silence notifications (except calls) while playing.",
                    isChecked = isFocusEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel.handleFocusModeToggle(context, isEnabled, notificationPermissionLauncher)
                    }
                )

                // This handles showing your existing dialogs for control/reset
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
                    ) }
            }
        }
    }
}


// --- Helper composables for a clean settings list ---
@Composable
fun SettingsItem(title: String, summary: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=0.7f))
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, summary: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha=0.7f))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}