package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors

@Composable
fun SessionTimerSection(
    durationSeconds: Long,
    isRecording: Boolean
) {
    val minutes = (durationSeconds / 60).toString().padStart(2, '0')
    val seconds = (durationSeconds % 60).toString().padStart(2, '0')

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SESSION DURATION",
            color = DashboardColors.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        
        Text(
            text = "$minutes:$seconds",
            color = DashboardColors.Navy,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) DashboardColors.Green else DashboardColors.Yellow)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isRecording) "Recording Active" else "Paused",
                color = if (isRecording) DashboardColors.Green else DashboardColors.Yellow,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
