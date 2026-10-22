package com.project.insole.features.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.HealthAndSafety
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
import com.project.insole.features.notifications.domain.OverallStatus
import com.project.insole.features.notifications.domain.PromptSeverity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Alerts tab — "support, not a medical alarm."
 *
 * Design system rule (docs/design/UX-BRAND-SPEC.md): everything here is built
 * from the home-tab brand — DashboardColors only, white cards radius 20,
 * navy hero card, 11sp uppercase section labels. No raw sensor numbers are
 * ever displayed; prompts name the foot and the action.
 */
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit = {}
) {
    val state = viewModel.notificationsState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardColors.Background)
    ) {
        // 1 ── Top App Bar (Centered with Back Button) ─────────────────────
        NotificationsTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // 2 ── Hero status card (mirrors home SystemStatusCard) ────────────
            HeroStatusCard(status = state.overallStatus)

            // 3 ── Guidance prompts ────────────────────────────────────────────
            if (state.prompts.isNotEmpty()) {
                Text(
                    text = "WHAT YOU CAN DO",
                    color = DashboardColors.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                state.prompts.forEach { prompt ->
                    GuidanceCard(prompt)
                }
            }

            // 4 ── History — hidden while empty (honest, not fabricated) ───────
            if (state.history.isNotEmpty()) {
                Text(
                    text = "RECENT NOTIFICATIONS",
                    color = DashboardColors.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(state.history) { notification ->
                        HistoryCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTopBar(onBack: () -> Unit) {
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
                text = "Alerts",
                color = DashboardColors.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gentle guidance, never alarms",
                color = DashboardColors.TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun HeroStatusCard(status: OverallStatus) {
    data class HeroVisual(val headline: String, val body: String, val iconBg: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

    val visual = when (status) {
        OverallStatus.ALL_CLEAR -> HeroVisual(
            "All clear",
            "Nothing needs your attention right now. Keep going — we're quietly watching over your feet.",
            DashboardColors.GreenMint,
            Icons.Default.Check
        )
        OverallStatus.ATTENTION -> HeroVisual(
            "Time to check",
            "There's something small you can do for your feet today. It takes just a minute.",
            DashboardColors.Yellow,
            Icons.Default.HealthAndSafety
        )
        OverallStatus.NO_DATA -> HeroVisual(
            "No recent data",
            "Your insoles aren't connected yet, so there's nothing to report — that's not the same as all clear.",
            DashboardColors.Brand,
            Icons.Default.BluetoothDisabled
        )
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "FOOT STATUS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.55.sp,
                )
                Text(
                    text = visual.headline,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                )
                Text(
                    text = visual.body,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 17.sp,
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

@Composable
private fun GuidanceCard(prompt: GuidancePrompt) {
    val iconBg = when (prompt.severity) {
        PromptSeverity.ATTENTION -> DashboardColors.Yellow
        PromptSeverity.INFO -> DashboardColors.Brand
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prompt.title,
                    color = DashboardColors.Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prompt.message,
                    color = DashboardColors.TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (prompt.actions.isNotEmpty()) {
                    Text(
                        text = "Try:",
                        color = DashboardColors.TextGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    prompt.actions.forEach { action ->
                        Text(
                            text = "•  $action",
                            color = DashboardColors.TextGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(notification: com.project.insole.features.notifications.domain.NotificationSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = notification.title,
                color = DashboardColors.Navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = notification.message,
                color = DashboardColors.TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = formatTime(notification.timestamp),
                color = DashboardColors.TextLightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm · dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
