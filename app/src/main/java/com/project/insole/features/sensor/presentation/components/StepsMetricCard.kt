package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.sensor.domain.model.WalkState
import java.util.Calendar

/**
 * Steps metric card showing daily steps and an HOURLY activity chart.
 * Displays 24 bars for the current day. Chart and Time Axis scroll together.
 */
@Composable
fun StepsMetricCard(
    stepCount: Int,
    hourlySteps: List<Int>, // List of 24 values
    walkState: WalkState,
    leftConnected: Boolean,
    rightConnected: Boolean,
    combinedAccelMag: Float, 
    stepGoal: Int = 10_000,
    modifier: Modifier = Modifier,
) {
    val maxStepInHour = hourlySteps.maxOrNull()?.coerceAtLeast(100) ?: 100
    val scrollState = rememberScrollState()
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val barWidth = 10.dp
    val barSpacing = 6.dp
    val totalBarWidth = barWidth + barSpacing

    // Auto-scroll to current hour
    LaunchedEffect(Unit) {
        val targetScroll = (currentHour * totalBarWidth.value).toInt()
        scrollState.scrollTo(targetScroll)
    }

    Card(
        modifier  = modifier.height(203.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text          = "DAILY STEP",
                    color         = DashboardColors.TextMuted,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 0.55.sp,
                )
                WalkStateBadge(state = walkState)
            }

            // Circular progress
            Box(
                modifier         = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress    = (stepCount.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f),
                    modifier    = Modifier.fillMaxSize(),
                    color       = DashboardColors.GreenMint,
                    trackColor  = DashboardColors.ProgressTrack,
                    strokeWidth = 6.dp,
                    strokeCap   = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint               = DashboardColors.StepBlue,
                        modifier           = Modifier.size(12.dp),
                    )
                    Text(
                        text       = "%,d".format(stepCount),
                        color      = DashboardColors.StepBlue,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text       = "/ $stepGoal",
                        color      = DashboardColors.TextLightGray,
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }

            // Scrollable Section (Bars + Time Labels)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(scrollState)
            ) {
                // 1. The Bars
                Row(
                    modifier = Modifier.height(32.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(barSpacing)
                ) {
                    hourlySteps.forEachIndexed { index, steps ->
                        val isCurrentHour = index == currentHour
                        MiniBar(
                            heightFraction = steps.toFloat() / maxStepInHour.toFloat(),
                            barWidth = barWidth,
                            isHighlighted = isCurrentHour
                        )
                    }
                }

                // 2. The Time Axis (Pinned to specific bar indices)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    val labels = listOf(
                        0 to "12 AM",
                        6 to "6 AM",
                        12 to "12 PM",
                        18 to "6 PM",
                        23 to "11 PM"
                    )
                    
                    labels.forEach { (hourIndex, label) ->
                        Text(
                            text = label,
                            color = DashboardColors.TextLightGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.offset(x = (hourIndex * totalBarWidth.value).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkStateBadge(state: WalkState) {
    val (label, color) = when (state) {
        WalkState.STANDING   -> "Standing" to DashboardColors.TextMuted
        WalkState.TRANSITION -> "Moving…"  to DashboardColors.GreenMint
        WalkState.WALKING    -> "Walking"  to DashboardColors.StepBlue
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun MiniBar(
    heightFraction: Float, 
    barWidth: Dp,
    isHighlighted: Boolean
) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .fillMaxHeight(heightFraction.coerceIn(0.1f, 1f))
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .background(if (isHighlighted) DashboardColors.StepBlue else DashboardColors.GreenBar)
    )
}
