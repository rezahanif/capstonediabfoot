package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Stateless composable that displays step count.
 * Shows total steps and step rate information.
 */
@Composable
fun StepCounterDisplay(stepCount: Int) {
    Column {
        Text(
            text = "Steps",
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stepCount.toString(),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "steps today",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
