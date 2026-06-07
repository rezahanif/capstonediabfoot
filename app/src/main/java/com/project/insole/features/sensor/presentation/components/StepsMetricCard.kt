package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

/**
 * Steps metric card showing daily steps and a rolling activity chart.
 * Display-only: expects pre-calculated step counts and walk state.
 */
@Composable
fun StepsMetricCard(
    stepCount: Int,
    walkState: WalkState,
    leftConnected: Boolean,
    rightConnected: Boolean,
    combinedAccelMag: Float, // Provided from ViewModel for visual bar chart
    stepGoal: Int = 10_000,
    modifier: Modifier = Modifier,
) {
    // ── Rolling bar chart display logic ───
    val barWindow = remember {
        ArrayDeque<Float>(14).also { dq -> repeat(14) { dq.add(0.01f) } }
    }

    LaunchedEffect(combinedAccelMag) {
        barWindow.removeFirst()
        barWindow.addLast(combinedAccelMag.coerceAtLeast(0.01f))
    }

    val barHeights = barWindow.toList()
    val maxBar     = barHeights.maxOrNull()?.coerceAtLeast(0.2f) ?: 0.2f

    Card(
        modifier  = modifier.height(203.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {

                }
                WalkStateBadge(state = walkState)
            }

            // Circular progress
            Box(
                modifier         = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress    = (stepCount.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f),
                    modifier    = Modifier.fillMaxSize(),
                    color       = DashboardColors.GreenMint,
                    trackColor  = DashboardColors.ProgressTrack,
                    strokeWidth = 8.dp,
                    strokeCap   = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint               = DashboardColors.StepBlue,
                        modifier           = Modifier.size(14.dp),
                    )
                    Text(
                        text       = "%,d".format(stepCount),
                        color      = DashboardColors.StepBlue,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text       = "/ $stepGoal steps",
                        color      = DashboardColors.TextLightGray,
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }

            // Bar chart
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                barHeights.forEach { h -> MiniBar(heightFraction = h / maxBar) }
            }

            // Time axis
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM").forEach { label ->
                    Text(
                        text       = label,
                        color      = DashboardColors.TextLightGray,
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionDot(connected: Boolean, label: String) {
    val dotColor = if (connected) DashboardColors.GreenMint else DashboardColors.TextMuted
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor.copy(alpha = if (connected) 1f else 0.3f))
        )
        Text(
            text       = label,
            color      = dotColor.copy(alpha = if (connected) 1f else 0.4f),
            fontSize   = 8.sp,
            fontWeight = FontWeight.Bold,
        )
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
private fun MiniBar(heightFraction: Float, barWidth: Dp = 6.dp) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .fillMaxHeight(heightFraction.coerceIn(0.05f, 1f))
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .background(DashboardColors.GreenBar)
    )
}
