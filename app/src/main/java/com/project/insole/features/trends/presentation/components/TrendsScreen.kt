package com.project.insole.features.trends.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.trends.presentation.DayActivity
import com.project.insole.features.trends.presentation.DayThermalMetric
import com.project.insole.features.trends.presentation.ThermalStatus
import com.project.insole.features.trends.presentation.TrendsViewModel

/**
 * Trends tab — "your week at a glance", built from real local history.
 *
 * Brand rule (docs/design/UX-BRAND-SPEC.md): home-tab tokens only. Plain
 * words over numbers where it matters; steps are activity feedback (already
 * shown on Home), not medical raw data, so they stay visible.
 */
@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel,
    onBack: () -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadTrends()
    }
    val state = viewModel.trendsState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardColors.Background)
    ) {
        // 1 ── Top App Bar (Centered with Back Button) ─────────────────────
        TrendsTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // 2 ── Week summary hero card ──────────────────────────────────────
            WeekSummaryCard(state)

            // 3 ── Last 7 days ─────────────────────────────────────────────────
            Text(
                text = "LAST 7 DAYS",
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
            )

            if (state.totalSteps == 0) {
                EmptyHistoryCard()
            } else {
                WeekListCard(state.week, state.totalSteps)
            }

            // 4 ── Thermal Balance (TD-7) ──────────────────────────────────────
            Text(
                text = "THERMAL BALANCE",
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
            )

            ThermalBalanceCard(state.thermalWeek, state.overallThermalStatus)

            // 5 ── Daily Wear Time Adherence ───────────────────────────────────
            Text(
                text = "DAILY WEAR TIME",
                color = DashboardColors.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
            )

            DailyWearTimeCard(
                averageWearMinutesPerDay = state.averageWearMinutesPerDay,
                daysWithWearCount = state.daysWithWearCount,
                totalWearMinutes = state.totalWearMinutes
            )

            // 6 ── Supportive footnote ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DashboardColors.InfoBadgeBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Keep your insoles on during the day — the more you wear them, " +
                        "the better this page understands your routine.",
                    color = DashboardColors.TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun TrendsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(73.dp)
            .background(DashboardColors.Background)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DashboardColors.Navy
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Trends",
                color = DashboardColors.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your week at a glance",
                color = DashboardColors.TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun WeekSummaryCard(state: com.project.insole.features.trends.presentation.TrendsUiState) {
    val takeaway = when {
        state.totalSteps == 0 -> "Your week is just getting started."
        state.activeDays == 1 -> "You moved on one day so far — every bit counts."
        else -> "You moved on ${state.activeDays} of the last 7 days" +
            (state.mostActiveDay?.let { ", with ${it.dayLabel.lowercase()} your busiest" } ?: "") + "."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(151.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.StatusCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "THIS WEEK",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.55.sp,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatSteps(state.totalSteps),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = " steps",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = takeaway,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
    }
}

@Composable
private fun WeekListCard(week: List<DayActivity>, totalSteps: Int) {
    val maxSteps = week.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            week.forEach { day ->
                DayRow(day, maxSteps)
            }
        }
    }
}

@Composable
private fun DayRow(day: DayActivity, maxSteps: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.dayLabel,
                color = if (day.isToday) DashboardColors.Brand else DashboardColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(0.25f)
            )
            Text(
                text = formatSteps(day.steps),
                color = DashboardColors.Navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.25f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DashboardColors.ProgressTrack)
        ) {
            val fraction = if (maxSteps == 0) 0f else day.steps.toFloat() / maxSteps
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DashboardColors.GreenBar)
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "Gathering your weekly trends.\n\nWear your insoles daily to see your activity summary here.",
            color = DashboardColors.TextMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun ThermalBalanceCard(
    thermalWeek: List<DayThermalMetric>,
    overallStatus: ThermalStatus
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status row with indicator dot
            val (dotColor, statusTitle, statusDesc) = when (overallStatus) {
                ThermalStatus.NO_DATA -> Triple(
                    DashboardColors.ProgressTrack,
                    "Not enough data yet",
                    "Wear both insoles during the day to track temperature balance across both feet."
                )
                ThermalStatus.BALANCED -> Triple(
                    DashboardColors.Green,
                    "Normal thermal balance",
                    "Skin temperature remained consistent between both feet this week."
                )
                ThermalStatus.MILD_WARMTH -> Triple(
                    DashboardColors.Yellow,
                    "Mild warmth observed",
                    "A slight temperature difference was noted between feet. Continue wearing and monitoring."
                )
                ThermalStatus.ATTENTION -> Triple(
                    DashboardColors.Yellow,
                    "Time to check feet",
                    "A noticeable temperature difference was recorded. Take a moment to inspect your feet for redness or irritation."
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Text(
                    text = statusTitle,
                    color = DashboardColors.Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = statusDesc,
                color = DashboardColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // 7-day dot timeline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                thermalWeek.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = day.dayLabel,
                            color = if (day.isToday) DashboardColors.Brand else DashboardColors.TextLightGray,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
                        )

                        val dayDotColor = when (day.status) {
                            ThermalStatus.NO_DATA -> DashboardColors.ProgressTrack
                            ThermalStatus.BALANCED -> DashboardColors.Green
                            ThermalStatus.MILD_WARMTH -> DashboardColors.Yellow
                            ThermalStatus.ATTENTION -> DashboardColors.Yellow
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dayDotColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyWearTimeCard(
    averageWearMinutesPerDay: Int,
    daysWithWearCount: Int,
    totalWearMinutes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val hours = averageWearMinutesPerDay / 60
            val mins = averageWearMinutesPerDay % 60
            val wearTimeDisplay = when {
                hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                hours > 0 -> "${hours}h"
                mins > 0 -> "${mins}m"
                else -> "0m"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wearTimeDisplay,
                        color = DashboardColors.Navy,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (daysWithWearCount > 0) "Daily average on active days" else "No wear recorded this week",
                        color = DashboardColors.TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Days worn badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DashboardColors.InfoBadgeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$daysWithWearCount of 7 days",
                        color = DashboardColors.Brand,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Progress bar toward recommended daytime wear (e.g. 6 hours / 360 mins)
            val targetMinutes = 360f
            val fraction = (averageWearMinutesPerDay / targetMinutes).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DashboardColors.ProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DashboardColors.Brand)
                )
            }
        }
    }
}

private fun formatSteps(steps: Int): String = java.text.NumberFormat
    .getNumberInstance(java.util.Locale.getDefault()).format(steps)
