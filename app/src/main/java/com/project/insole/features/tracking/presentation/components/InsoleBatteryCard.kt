package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors

/**
 * Insole Battery & Power Status Card.
 * Honest UX (TD-13 & Brand Spec R7):
 * - When battery telemetry is available (> 0), renders numeric percentage.
 * - When hardware monitoring is not yet installed, honestly renders "Active" (Powered) instead of fake 100%.
 * - Real bilateral pressure balance: "Balanced" vs "Uneven" computed from live peak pressures.
 */
@Composable
fun InsoleBatteryCard(
    side: String,
    batteryPct: Int? = null,
    isConnected: Boolean,
    pressureBalanceText: String = "N/A",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val statusColor = if (isConnected) DashboardColors.Green else Color.Gray
    val statusText = if (isConnected) "Connected" else "Disconnected"

    // Honest battery representation (TD-13)
    val hasRealBatteryNumber = batteryPct != null && batteryPct in 1..100
    val displayPct = if (hasRealBatteryNumber) batteryPct!! else 0

    val balanceBg = when (pressureBalanceText) {
        "Balanced" -> DashboardColors.GreenMint
        "Uneven" -> DashboardColors.Yellow
        else -> Color.Gray
    }

    Card(
        modifier = modifier
            .height(242.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = side,
                        color = DashboardColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details",
                    tint = if (isConnected) DashboardColors.Brand else DashboardColors.TextLightGray,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Circular progress / Power Status
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = when {
                        !isConnected -> 0f
                        hasRealBatteryNumber -> displayPct / 100f
                        else -> 1f // Steady full ring for active powered insole
                    },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isConnected) DashboardColors.GreenMint else DashboardColors.TextLightGray,
                    trackColor = DashboardColors.ProgressTrack,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!isConnected) {
                        Text(
                            text = "--",
                            color = Color.Gray,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    } else if (hasRealBatteryNumber) {
                        Text(
                            text = "$displayPct%",
                            color = DashboardColors.Navy,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    } else {
                        // Honest "Active" state when powered without ADC gauge (TD-13)
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Powered",
                            tint = DashboardColors.GreenMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Active",
                            color = DashboardColors.Navy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Powered",
                            color = DashboardColors.TextGray,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            // Pressure Balance badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DashboardColors.InfoBadgeBg)
                    .padding(13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Pressure\nBalance",
                    color = DashboardColors.TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp,
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isConnected) balanceBg else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (isConnected) pressureBalanceText else "N/A",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
