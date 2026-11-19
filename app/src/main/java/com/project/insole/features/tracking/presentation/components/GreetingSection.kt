package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import java.time.LocalTime

/**
 * Dashboard Header Greeting.
 * Dynamically computes time-of-day greeting (Morning / Afternoon / Evening).
 * Accurately reflects connection badge (MONITORING ACTIVE vs CONNECTING vs OFFLINE)
 * and latest packet check timestamp.
 */
@Composable
fun GreetingSection(
    userName: String = "Reza",
    lastCheckTime: String? = null,
    isConnected: Boolean = false,
    isConnecting: Boolean = false
) {
    val hour = LocalTime.now().hour
    val greetingPrefix = when (hour) {
        in 4..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val (badgeBg, badgeText) = when {
        isConnecting -> DashboardColors.Brand to "CONNECTING..."
        isConnected -> DashboardColors.Green to "MONITORING ACTIVE"
        else -> DashboardColors.TextGray to "OFFLINE"
    }

    val checkText = when {
        lastCheckTime != null -> "Last check: $lastCheckTime"
        isConnected -> "Streaming live"
        else -> "Insoles not connected"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$greetingPrefix, $userName",
            color = DashboardColors.Navy,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.56).sp,
            lineHeight = 36.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Status Badge
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.55.sp,
                )
            }
            Text(
                text = checkText,
                color = DashboardColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
