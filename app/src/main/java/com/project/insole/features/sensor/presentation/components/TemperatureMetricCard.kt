package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

@Composable
fun TemperatureMetricCard(
    leftTempC: Float,
    rightTempC: Float,
    modifier: Modifier = Modifier,
) {
    val tempDiff = String.format("%.1f", kotlin.math.abs(leftTempC - rightTempC))

    Card(
        modifier = modifier.height(203.dp),
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
            Text(
                text = "LEFT INSOLE",
                modifier = Modifier.fillMaxWidth(),
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.55.sp,
            )

            // Feet illustration + temp readings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Left",
                        color = DashboardColors.TextGray,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "%.1f°C".format(leftTempC),
                        color = DashboardColors.TempGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Feet icon placeholder
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = DashboardColors.Brand,
                        modifier = Modifier.size(width = 16.dp, height = 40.dp),
                    )
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = DashboardColors.Brand,
                        modifier = Modifier.size(width = 16.dp, height = 40.dp),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Right",
                        color = DashboardColors.TextGray,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "%.1f°C".format(rightTempC),
                        color = DashboardColors.TempBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Difference badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DashboardColors.InfoBadgeBg)
                    .padding(13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Difference",
                    color = DashboardColors.TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = "${tempDiff}°C",
                    color = DashboardColors.StepBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
