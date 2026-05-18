package com.storagedoctor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StorageProgressIndicator(
    totalStorage: Float,
    usedStorage: Float,
    recoverableStorage: Float
) {
    val usedPercentage = usedStorage / totalStorage

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { usedPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = if (usedPercentage > 0.8f)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total", style = MaterialTheme.typography.labelSmall)
                Text("${totalStorage.toInt()} GB", style = MaterialTheme.typography.bodyLarge)
            }
            Column {
                Text("Used", style = MaterialTheme.typography.labelSmall)
                Text("${usedStorage.toInt()} GB", style = MaterialTheme.typography.bodyLarge)
            }
            Column {
                Text("Recoverable", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${recoverableStorage} GB",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
