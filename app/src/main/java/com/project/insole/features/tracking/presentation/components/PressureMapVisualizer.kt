package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.insole.features.tracking.domain.model.PressureZone

/**
 * Stateless composable that draws the heatmap visualization of foot pressure.
 * Receives pressure grid data and renders colored cells based on pressure intensity.
 */
@Composable
fun PressureMapVisualizer(pressureGrid: List<List<PressureZone>>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        pressureGrid.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { zone ->
                    PressureCell(zone)
                }
            }
        }
    }
}

@Composable
private fun PressureCell(zone: PressureZone) {
    val cellColor = when (zone.pressure) {
        in 0..50 -> Color.Blue
        in 51..100 -> Color.Cyan
        in 101..150 -> Color.Green
        in 151..200 -> Color.Yellow
        in 201..250 -> Color.Magenta
        else -> Color.Red
    }

    Spacer(
        modifier = Modifier
            .size(20.dp)
            .background(cellColor)
            .aspectRatio(1f)
    )
}
