package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.sensor.domain.model.SensorConstants

/**
 * Temperature asymmetry status card for the patient Live Monitor screen.
 * Strictly adheres to UX-BRAND-SPEC Rule R1 (no raw ΔT numbers to patients;
 * raw clinical values are preserved in Doctor Diagnostics).
 * Rule R5 (no fake all-clear on empty data) and R6 (calm Yellow for attention).
 */
@Composable
fun TempAsymmetryCard(
    leftTemp: Float,
    rightTemp: Float,
    modifier: Modifier = Modifier
) {
    val diff = kotlin.math.abs(leftTemp - rightTemp)
    val hasBothReadings = leftTemp > 0f && rightTemp > 0f

    val (pillColor, textColor, label, icon) = when {
        !hasBothReadings -> Quad(
            DashboardColors.ProgressTrack,
            DashboardColors.TextMuted,
            "NO DATA",
            Icons.Default.Info
        )
        diff >= SensorConstants.TEMPERATURE_DIFF_WARNING -> Quad(
            DashboardColors.Yellow,
            Color.White,
            "TIME TO CHECK",
            Icons.Default.Warning
        )
        else -> Quad(
            DashboardColors.GreenMint,
            Color.White,
            "BALANCED",
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(84.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 21.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TEMPERATURE BALANCE",
                    color = DashboardColors.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (hasBothReadings) "Bilateral insole thermal status" else "Awaiting readings from both feet",
                    color = DashboardColors.TextLightGray,
                    fontSize = 12.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = pillColor,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
