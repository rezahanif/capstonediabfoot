package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors

@Composable
fun StepsMetricCard(
    stepCount: Int,
    stepGoal: Int,
    modifier: Modifier = Modifier,
) {
    // Mini bar chart data
    val barHeights = listOf(6.4f, 3.2f, 9.6f, 4.8f, 19.2f, 25.6f, 12.8f, 16f, 9.6f, 28.8f, 32f, 22.4f, 12.8f, 3.2f)
    val maxBar = barHeights.max()

    Card(
        modifier = modifier.height(203.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "RIGHT INSOLE",
                modifier = Modifier.fillMaxWidth(),
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.55.sp,
            )

            // Circular step progress
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = stepCount.toFloat() / stepGoal.toFloat(),
                    modifier = Modifier.fillMaxSize(),
                    color = DashboardColors.GreenMint,
                    trackColor = DashboardColors.ProgressTrack,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = DashboardColors.StepBlue,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "%,d".format(stepCount),
                        color = DashboardColors.StepBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "/ $stepGoal steps",
                        color = DashboardColors.TextLightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }

            // Mini bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                barHeights.forEach { h ->
                    MiniBar(heightFraction = h / maxBar)
                }
            }

            // Time axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM").forEach { label ->
                    Text(
                        text = label,
                        color = DashboardColors.TextLightGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniBar(heightFraction: Float, barWidth: Dp = 6.dp) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .fillMaxHeight(heightFraction.coerceIn(0.05f, 1f))
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .background(DashboardColors.GreenBar)
    )
}
