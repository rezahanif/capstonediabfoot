package com.project.insole.features.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings screen — home-tab brand (docs/design/UX-BRAND-SPEC.md).
 *
 * Sections: MY DOCTOR (pairing code entry / linked card), DEVICE (insole
 * pairing), ALERTS (honest: thresholds are clinical constants), ABOUT.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {}
) {
    val state = viewModel.settingsState.collectAsState().value
    val context = LocalContext.current
    var showUnlinkDialog by remember { mutableStateOf(false) }

    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = {
                Text(
                    text = "Unlink Doctor?",
                    fontWeight = FontWeight.Bold,
                    color = DashboardColors.Navy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "You will need a new clinic code to reconnect.",
                    color = DashboardColors.TextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        viewModel.unlinkDoctor()
                    }
                ) {
                    Text(
                        text = "Unlink",
                        color = DashboardColors.Alert,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel", color = DashboardColors.Navy)
                }
            },
            containerColor = DashboardColors.CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DashboardColors.Background)
            .padding(bottom = 24.dp)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
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
                text = "Settings",
                color = DashboardColors.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ── MY DOCTOR ─────────────────────────────────────────────────────
        SectionLabel("MY DOCTOR")
        if (state.doctor.isLinked) {
            DoctorLinkedCard(
                doctorName = state.doctor.doctorName.ifEmpty { "Your doctor" },
                code = state.doctor.code,
                pairedAt = state.doctor.pairedAt,
                pending = state.doctor.pendingServerConfirmation,
                onUnlink = { showUnlinkDialog = true }
            )
        } else {
            DoctorLinkCard(
                codeInput = state.codeInput,
                codeError = state.codeError,
                onCodeChange = viewModel::onCodeInputChange,
                onLink = viewModel::linkDoctor
            )
        }

        // ── DEVICE ────────────────────────────────────────────────────────
        SectionLabel("DEVICE")
        SettingsNavCard(
            title = "Paired insoles",
            subtitle = "Connect or re-pair your left and right insole",
            onClick = onNavigateToPairing
        )
        Spacer(Modifier.height(8.dp))
        SettingsNavCard(
            title = "Hardware & clinical diagnostics",
            subtitle = "Live signal RSSI, raw sensor telemetry & firmware status",
            onClick = onNavigateToDiagnostics
        )

        // ── DATA EXPORT ────────────────────────────────────────────────────
        SectionLabel("HEALTH DATA EXPORT")
        DataExportCard(
            isExporting = state.isExporting,
            onExportJson = { viewModel.exportData(context, isJson = true) },
            onExportCsv = { viewModel.exportData(context, isJson = false) }
        )

        // ── ALERTS ────────────────────────────────────────────────────────
        SectionLabel("ALERTS")
        InfoCard(
            body = "Alert thresholds are preset based on clinical guidelines to monitor persistent temperature differences of 2.2°C between feet or sustained high pressure during walking.\n\nA single unusual reading will not trigger an alert — only lasting changes do."
        )

        // ── ABOUT ─────────────────────────────────────────────────────────
        SectionLabel("ABOUT")
        InfoCard(
            body = "SmartInsole 1.0.0\nYour data stays on this phone unless you link " +
                "a doctor. Summaries shared with a doctor never include a diagnosis."
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = DashboardColors.TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun DoctorLinkCard(
    codeInput: String,
    codeError: String?,
    onCodeChange: (String) -> Unit,
    onLink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DashboardColors.Brand),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Link your doctor",
                    color = DashboardColors.Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Your doctor can share a pairing code with you. Enter it here to " +
                    "share your foot-health summaries with them — only one doctor can " +
                    "be linked at a time.",
                color = DashboardColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            OutlinedTextField(
                value = codeInput,
                onValueChange = onCodeChange,
                singleLine = true,
                placeholder = { Text("e.g. 4AB7-9CD2", color = DashboardColors.TextLightGray, fontSize = 14.sp) },
                isError = codeError != null,
                supportingText = codeError?.let {
                    { Text(it, color = DashboardColors.Alert, fontSize = 12.sp) }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DashboardColors.Brand,
                    unfocusedBorderColor = DashboardColors.ProgressTrack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            Button(
                onClick = onLink,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Link doctor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DoctorLinkedCard(
    doctorName: String,
    code: String,
    pairedAt: Long,
    pending: Boolean,
    onUnlink: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DashboardColors.GreenMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = doctorName,
                        color = DashboardColors.Navy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pairing code $code",
                        color = DashboardColors.TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Linked " + SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                            .format(Date(pairedAt)),
                        color = DashboardColors.TextLightGray,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = if (pending) "Waiting for your doctor's confirmation — summaries " +
                    "will start syncing once it goes through."
                else "Your foot-health summaries are shared with this doctor.",
                color = DashboardColors.TextGray,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            TextButton(onClick = onUnlink) {
                Text("Unlink", color = DashboardColors.Alert, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsNavCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = DashboardColors.Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = DashboardColors.TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DashboardColors.TextLightGray
            )
        }
    }
}

@Composable
private fun InfoCard(body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = body,
            color = DashboardColors.TextMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun DataExportCard(
    isExporting: Boolean,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardColors.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DashboardColors.Brand),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Export Health Data",
                        color = DashboardColors.Navy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Save or share your weekly records",
                        color = DashboardColors.TextLightGray,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = "Download your 7-day foot health records to share with your clinic. You can export a clinical summary report or full data records.",
                color = DashboardColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExportJson,
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DashboardColors.Brand),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onExportCsv,
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DashboardColors.Brand),
                    border = BorderStroke(1.dp, DashboardColors.Brand),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
