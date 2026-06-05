package com.project.insole.features.tracking.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.R
import com.project.insole.core.theme.DashboardColors

@Composable
fun TemperatureMetricCard(
    leftTempC: Float,
    rightTempC: Float,
    leftConnected: Boolean,
    rightConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    // ✅ No rawBle strings needed, no SensorPacket re-parsing here.
    // Values come pre-parsed from ViewModel for maximum performance.
    val isConnected = leftConnected || rightConnected
    val tempDiff = String.format("%.1f", kotlin.math.abs(leftTempC - rightTempC))

    // Helper function to get image resource based on temperature
    fun getFootImage(temp: Float): Int {
        if (!isConnected) return R.drawable.abu
        return when {
            temp < 20f -> R.drawable.biru
            temp in 20f..25f -> R.drawable.hijau
            temp in 25.1f..30f -> R.drawable.kuning
            else -> R.drawable.merah
        }
    }

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
                text = "TEMPERATURE",
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

                // Feet icons based on temperature
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Image(
                        painter = painterResource(id = getFootImage(leftTempC)),
                        contentDescription = "Left foot temperature state",
                        modifier = Modifier.size(width = 21.6.dp, height = 45.dp),
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(id = getFootImage(rightTempC)),
                        contentDescription = "Right foot temperature state",
                        modifier = Modifier
                            .size(width = 21.6.dp, height = 45.dp)
                            .scale(scaleX = -1f, scaleY = 1f), // Mirror the right foot
                        contentScale = ContentScale.Fit
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
            Spacer(Modifier.height(2.dp))
            // Difference badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DashboardColors.InfoBadgeBg)
                    .padding(18.dp),
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
