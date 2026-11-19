package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.notifications.domain.OverallStatus

/**
 * System Status hero card on the Home dashboard.
 * Enforces UX-BRAND-SPEC Rule R5: "No recent data is never shown as All Clear."
 * Dynamically reflects connection state, thermal/pressure alerts, or clean status.
 */
@Composable
fun SystemStatusCard(
    status: OverallStatus = OverallStatus.ALL_CLEAR,
    isConnecting: Boolean = false
) {
    data class StatusVisual(
        val headline: String,
        val body: String,
        val iconBg: Color,
        val icon: ImageVector,
        val showSpinner: Boolean = false
    )

    val visual = when {
        isConnecting -> StatusVisual(
            headline = "Connecting...",
            body = "Searching for and pairing with your smart insoles...",
            iconBg = DashboardColors.Brand,
            icon = Icons.Default.Sync,
            showSpinner = true
        )
        status == OverallStatus.NO_DATA -> StatusVisual(
            headline = "No data yet today",
            body = "Put your insoles on to begin monitoring.",
            iconBg = DashboardColors.Brand,
            icon = Icons.Default.BluetoothDisabled
        )
        status == OverallStatus.ATTENTION -> StatusVisual(
            headline = "Time to check",
            body = "Mild asymmetry detected. Take a quiet moment to inspect your feet.",
            iconBg = DashboardColors.Yellow,
            icon = Icons.Default.HealthAndSafety
        )
        else -> StatusVisual(
            headline = "All clear",
            body = "No anomalies detected in your gait or pressure points today.",
            iconBg = DashboardColors.GreenMint,
            icon = Icons.Default.Check
        )
    }

    val headlineFontSize = if (visual.headline.length > 12) 22.sp else 28.sp
    val headlineLineHeight = if (visual.headline.length > 12) 28.sp else 34.sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .heightIn(min = 151.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.StatusCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 151.dp)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "SYSTEM STATUS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.55.sp,
                )
                Text(
                    text = visual.headline,
                    color = Color.White,
                    fontSize = headlineFontSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = headlineLineHeight,
                )
                Text(
                    text = visual.body,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(visual.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                if (visual.showSpinner) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = visual.headline,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
