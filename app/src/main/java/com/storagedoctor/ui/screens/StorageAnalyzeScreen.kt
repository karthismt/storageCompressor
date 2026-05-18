package com.storagedoctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzeScreen(
    onStartCompression: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanComplete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Analysis") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isScanning && !scanComplete) {
                // Pre-scan state
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan Your Storage",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Detect large photos and videos that can be safely compressed.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { isScanning = true }) {
                            Text("Start Scan")
                        }
                    }
                }
            } else if (isScanning) {
                // Scanning state
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Scanning device storage...")
                        Text(
                            text = "This may take a moment",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Simulate scan completion
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    isScanning = false
                    scanComplete = true
                }
            } else {
                // Results
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scan Results",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Images Found", style = MaterialTheme.typography.labelMedium)
                                Text("1,234 files (8.2 GB)", style = MaterialTheme.typography.bodyLarge)
                            }
                            Icon(Icons.Default.Image, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Videos Found", style = MaterialTheme.typography.labelMedium)
                                Text("45 files (15.3 GB)", style = MaterialTheme.typography.bodyLarge)
                            }
                            Icon(Icons.Default.VideoFile, contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary)
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Estimated savings: ~12.5 GB",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Compression Mode Selection
                Text(
                    text = "Compression Mode",
                    style = MaterialTheme.typography.titleMedium
                )

                var selectedMode by remember { mutableIntStateOf(1) }
                val modes = listOf("High Quality", "Balanced", "Maximum Saving")

                modes.forEachIndexed { index, mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMode == index,
                            onClick = { selectedMode = index }
                        )
                        Text(mode)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onStartCompression,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Compression")
                }
            }
        }
    }
}
