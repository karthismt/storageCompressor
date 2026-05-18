package com.storagedoctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var compressionMode by remember { mutableIntStateOf(1) }
    var backgroundEnabled by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(false) }
    var restoreBackupDays by remember { mutableFloatStateOf(7f) }
    var mediaAgeDays by remember { mutableFloatStateOf(15f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compression Quality
            Text("Compression Quality", style = MaterialTheme.typography.titleMedium)
            val modes = listOf("High Quality", "Balanced", "Maximum Saving")
            modes.forEachIndexed { index, mode ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = compressionMode == index,
                        onClick = { compressionMode = index }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(mode, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            when (index) {
                                0 -> "Minimal quality loss, moderate space saving"
                                1 -> "Good balance between quality and space"
                                else -> "Maximum space saving, slight quality reduction"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            HorizontalDivider()

            // Background Scheduling
            Text("Background Scheduling", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable background compression")
                Switch(checked = backgroundEnabled, onCheckedChange = { backgroundEnabled = it })
            }

            if (backgroundEnabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Compression runs only when:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("• Phone is charging", style = MaterialTheme.typography.bodySmall)
                        Text("• Battery > 40%", style = MaterialTheme.typography.bodySmall)
                        Text("• Screen is off", style = MaterialTheme.typography.bodySmall)
                        Text("• Device is idle", style = MaterialTheme.typography.bodySmall)
                        Text("• Thermal state is normal", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            HorizontalDivider()

            // Media Age
            Text("Media Age Threshold", style = MaterialTheme.typography.titleMedium)
            Text(
                "Only compress files older than ${mediaAgeDays.toInt()} days",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = mediaAgeDays,
                onValueChange = { mediaAgeDays = it },
                valueRange = 7f..60f,
                steps = 52
            )

            HorizontalDivider()

            // Restore Backup
            Text("Restore Backup", style = MaterialTheme.typography.titleMedium)
            Text(
                "Keep original files for ${restoreBackupDays.toInt()} days after compression",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = restoreBackupDays,
                onValueChange = { restoreBackupDays = it },
                valueRange = 1f..30f,
                steps = 28
            )

            HorizontalDivider()

            // About
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text("Storage Doctor v1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Save storage safely with verified compression.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
