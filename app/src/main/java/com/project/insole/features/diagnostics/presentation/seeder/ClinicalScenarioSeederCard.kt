package com.project.insole.features.diagnostics.presentation.seeder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.notifications.domain.FootSide
import com.project.insole.features.sensor.domain.model.RiskLevel

/**
 * Dedicated UI card in the presentation layer for clinical verification and scenario seeding.
 * Enables doctors and developers to test multi-day clinical logic (e.g., Armstrong 2-day
 * consecutive hotspot and Valid Swell scoring) without waiting 24–48 real calendar hours.
 */
@Composable
fun ClinicalScenarioSeederCard(
    viewModel: ClinicalSeederViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = DashboardColors.Brand,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Clinical Scenario Seeder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardColors.Navy
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = DashboardColors.CardTintBlue,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = "TEST HELPER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashboardColors.Brand,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Seed yesterday's clinical metrics into Room DB to test 2-day consecutive hotspot and risk scoring in real time.",
                fontSize = 12.sp,
                color = DashboardColors.TextGray,
                lineHeight = 16.sp
            )

            // ── Status Readout ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DashboardColors.Background
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Yesterday (${state.yesterdayDate}):",
                            fontSize = 12.sp,
                            color = DashboardColors.TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (state.hasYesterdayRecord) {
                                val foot = state.yesterdayWarmerFoot?.name ?: "NONE"
                                "$foot Hotspot (${String.format("%.1f", state.yesterdayAsymmetryC)}°C)"
                            } else "No Record (Empty)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.hasYesterdayRecord) DashboardColors.Brand else DashboardColors.TextLightGray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Consecutive Hotspot Days:",
                            fontSize = 12.sp,
                            color = DashboardColors.TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${state.consecutiveThermalDays} day(s) ${if (state.isSustainedHotspot2Day) "🔥 (DAY-2 TRIGGERED)" else ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isSustainedHotspot2Day) DashboardColors.Alert else DashboardColors.Navy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Doctor Risk Scoring:",
                            fontSize = 12.sp,
                            color = DashboardColors.TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (state.currentRiskLevel) {
                                RiskLevel.HIGH -> DashboardColors.Alert.copy(alpha = 0.15f)
                                RiskLevel.MEDIUM -> DashboardColors.Yellow.copy(alpha = 0.2f)
                                RiskLevel.LOW -> DashboardColors.GreenMint.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = "${state.currentRiskLevel.name} ${if (state.isValidSwell) "(VALID SWELL)" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (state.currentRiskLevel) {
                                RiskLevel.HIGH -> DashboardColors.Alert
                                RiskLevel.MEDIUM -> DashboardColors.Navy
                                RiskLevel.LOW -> DashboardColors.GreenMint
                            },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Status feedback message if any
            state.statusMessage?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = DashboardColors.InfoBadgeBg,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DashboardColors.Brand,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // ── Seeder Action Buttons ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.seedYesterdayLeftHotspot() },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Seed Left Hotspot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.seedYesterdayRightHotspot() },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Seed Right Hotspot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.seedYesterdayNormal() },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp), tint = DashboardColors.Green)
                        Spacer(Modifier.width(4.dp))
                        Text("Seed Baseline", fontSize = 11.sp, color = DashboardColors.Navy)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearSeededYesterday() },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = DashboardColors.Alert)
                        Spacer(Modifier.width(4.dp))
                        Text("Clear Yesterday", fontSize = 11.sp, color = DashboardColors.Alert)
                    }
                }
            }
        }
    }
}
