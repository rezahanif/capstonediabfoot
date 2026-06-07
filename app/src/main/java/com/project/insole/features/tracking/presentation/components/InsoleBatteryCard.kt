package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun InsoleBatteryCard(
    side: String,
    batteryPct: Int,
    isConnected: Boolean, // ✅ Added parameter
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val statusColor = if (isConnected) DashboardColors.Green else Color.Gray
    val statusText = if (isConnected) "Connected" else "Disconnected"

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
                                .background(statusColor) // ✅ Dynamic color
                        )
                        Text(
                            text = statusText, // ✅ Dynamic text
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Bluetooth",
                    tint = if (isConnected) Color(0xFF2563EB) else Color.Gray,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Circular progress
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = if (isConnected) batteryPct / 100f else 0f,
                    modifier = Modifier.fillMaxSize(),
                    color = if (isConnected) DashboardColors.GreenMint else Color.LightGray,
                    trackColor = DashboardColors.ProgressTrack,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isConnected) "$batteryPct%" else "--%",
                        color = if (isConnected) DashboardColors.Navy else Color.Gray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
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
                        .background(if (isConnected) DashboardColors.GreenMint else Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (isConnected) "Good" else "N/A",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
