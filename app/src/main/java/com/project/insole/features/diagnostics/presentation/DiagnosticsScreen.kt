package com.project.insole.features.diagnostics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
import com.project.insole.features.diagnostics.data.DiagnosticsDataSource.InsoleHardwareDiagnostic
import com.project.insole.features.diagnostics.domain.HealthStatus

import com.project.insole.features.diagnostics.presentation.seeder.ClinicalScenarioSeederCard
import com.project.insole.features.diagnostics.presentation.seeder.ClinicalSeederViewModel

/**
 * Diagnostics screen for Doctor & Engineering verification.
 * Preserves raw numbers (ΔT, Celsius, kPa, RSSI dBm) specifically for clinical
 * and hardware inspection, fulfilling the requirement "we keep the number for doctor".
 */
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    seederViewModel: ClinicalSeederViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBack: () -> Unit
) {
    val state = viewModel.diagnosticsState.collectAsState().value
    val report = state.diagnosticReport
    val status = report?.status

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardColors.Background)
    ) {
        // ── Top App Bar ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DashboardColors.Navy
                )
            }
            Text(
                text = "Hardware Diagnostics",
                color = DashboardColors.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { viewModel.refreshDiagnostics() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = DashboardColors.Brand
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Doctor Clinical Note Banner ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DashboardColors.InfoBadgeBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = DashboardColors.Brand,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Clinical Telemetry: Detailed raw numbers (ΔT, °C, kPa) are retained here for clinical evaluation and engineering analysis. Patient views display simplified qualitative health indicators.",
                        color = DashboardColors.TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            if (status != null) {
                // ── Bilateral Sensor Telemetry Cards ─────────────────────────
                SectionHeader("INSOLE TELEMETRY")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsoleDiagCard(
                        diag = status.leftInsole,
                        accentColor = DashboardColors.Brand,
                        modifier = Modifier.weight(1f)
                    )
                    InsoleDiagCard(
                        diag = status.rightInsole,
                        accentColor = DashboardColors.SideRight,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Clinical Thermal Asymmetry ───────────────────────────────
                SectionHeader("CLINICAL THERMAL ASYMMETRY")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Inter-Foot Difference (ΔT)",
                                color = DashboardColors.TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Δ %.2f °C".format(status.thermalAsymmetryC),
                                color = if (status.thermalAsymmetryC >= 2.2f) DashboardColors.Yellow else DashboardColors.Navy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = report.thermalAsymmetrySummary,
                            color = DashboardColors.TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Clinical reference: |ΔT| ≥ 2.2 °C sustained triggers thermal prompt (JMIR mHealth 2025 / IWGDF 2023).",
                            color = DashboardColors.TextLightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                // ── System & BLE Transport Health ─────────────────────────────
                SectionHeader("HARDWARE & TRANSPORT")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow("Overall System Health", report.overallHealth.name)
                        InfoRow("Signal Quality", report.signalQuality)
                        InfoRow("Gait State", "${status.walkState} (${status.totalSteps} steps)")
                        InfoRow("Payload Protocol", status.firmwareProtocol)
                        InfoRow("Battery Telemetry", status.batteryNotice)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (state.isLoading) "Reading insole diagnostics..." else "No diagnostic data available. Ensure insoles are paired and connected.",
                        color = DashboardColors.TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // ── Clinical Scenario Seeder (Test & Verification) ───────────
            SectionHeader("CLINICAL VERIFICATION & SCENARIO SEEDER")
            ClinicalScenarioSeederCard(viewModel = seederViewModel)

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InsoleDiagCard(
    diag: InsoleHardwareDiagnostic,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = diag.side.uppercase(),
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = diag.macAddress ?: "Not paired",
                color = DashboardColors.TextLightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))
            MetricLine("Status", diag.connectionState)
            MetricLine("RSSI", diag.rssiDbm?.let { "$it dBm" } ?: "Unknown")
            MetricLine("Live Temp", if (diag.liveTemperatureC > 0f) "%.2f °C".format(diag.liveTemperatureC) else "--")
            MetricLine("Peak Temp", if (diag.peakTemperatureC > 0f) "%.2f °C".format(diag.peakTemperatureC) else "--")
            MetricLine("Live Load", "%.1f kPa".format(diag.livePressureKpa))
            MetricLine("Peak Load", "%.1f kPa".format(diag.peakPressureKpa))
            MetricLine("Steps", "${diag.stepCount}")
        }
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = DashboardColors.TextMuted, fontSize = 11.sp)
        Text(text = value, color = DashboardColors.Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = DashboardColors.TextMuted, fontSize = 12.sp)
        Text(text = value, color = DashboardColors.Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = DashboardColors.TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}
