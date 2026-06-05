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
import com.project.insole.features.sensor.domain.model.SensorPacket   // ← single canonical class
import kotlin.math.abs
import kotlin.math.sqrt

// ════════════════════════════════════════════════════════════════════════════
// BLE CONSTANTS
// ════════════════════════════════════════════════════════════════════════════

object InsoleUUIDs {
    const val LEFT_SERVICE         = "4fa2c732-ca9a-4c20-9492-c167df3c942b"
    const val LEFT_CHARACTERISTIC  = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    const val RIGHT_SERVICE        = "4fa2c732-ca9a-4c20-9492-c167df3c942c"
    const val RIGHT_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26c9"

    /** Returns "LEFT", "RIGHT", or "UNKNOWN" given a service UUID string. */
    fun identifySide(serviceUuid: String?): String = when {
        serviceUuid?.equals(LEFT_SERVICE,  ignoreCase = true) == true -> "LEFT"
        serviceUuid?.equals(RIGHT_SERVICE, ignoreCase = true) == true -> "RIGHT"
        else -> "UNKNOWN"
    }
}

// ════════════════════════════════════════════════════════════════════════════
// FSM STATE
// ════════════════════════════════════════════════════════════════════════════

enum class WalkState { STANDING, TRANSITION, WALKING }

// ════════════════════════════════════════════════════════════════════════════
// WALKING FSM  (one instance per foot)
//
// Uses com.project.insole.features.sensor.domain.model.SensorPacket.
// Fields in that class:
//   accelX, accelY, accelZ  (m/s² from Adafruit_MPU6050)
//   gyroX,  gyroY,  gyroZ   (rad/s)
//   pressure                (0-255 raw ADC mapped by firmware)
//   temperature             (°C Steinhart-Hart)
// ════════════════════════════════════════════════════════════════════════════

class WalkingFSM {

    // ── EMA ──────────────────────────────────────────────────────────────────
    private val alpha = 0.2f
    private var filterInit = false

    private var axF = 0f; private var ayF = 0f; private var azF = 0f
    private var gxF = 0f; private var gyF = 0f; private var gzF = 0f

    // ── Motion features ───────────────────────────────────────────────────────
    var accelMag = 0f;  private set
    var gyroMag  = 0f;  private set

    private var previousTotalAccel = 1.0f

    // ── FSM ───────────────────────────────────────────────────────────────────
    var state: WalkState = WalkState.STANDING;  private set

    private var walkingTimerMs  = 0L
    private var standingTimerMs = 0L

    // ── Step counter ──────────────────────────────────────────────────────────
    var stepCount = 0;  private set

    private var stepTriggered = false
    private val stepThreshold = 0.10f

    // ─────────────────────────────────────────────────────────────────────────
    private fun ema(input: Float, prev: Float) = alpha * input + (1f - alpha) * prev

    fun process(packet: SensorPacket): Int {

        // ── Convert m/s² → g (thresholds were tuned in g-units) ──────────────
        // domain.model.SensorPacket uses accelX/Y/Z, gyroX/Y/Z
        val ax = packet.accelX / 9.81f
        val ay = packet.accelY / 9.81f
        val az = packet.accelZ / 9.81f
        val gx = packet.gyroX
        val gy = packet.gyroY
        val gz = packet.gyroZ

        // ── EMA filter ────────────────────────────────────────────────────────
        if (!filterInit) {
            axF = ax; ayF = ay; azF = az
            gxF = gx; gyF = gy; gzF = gz
            filterInit = true
        } else {
            axF = ema(ax, axF); ayF = ema(ay, ayF); azF = ema(az, azF)
            gxF = ema(gx, gxF); gyF = ema(gy, gyF); gzF = ema(gz, gzF)
        }

        // ── Combined motion energy (identical to firmware) ─────────────────────
        val totalAccel     = sqrt(axF * axF + ayF * ayF + azF * azF)
        val gravityRemoved = abs(totalAccel - 1.0f)
        val derivative     = abs(totalAccel - previousTotalAccel)
        previousTotalAccel = totalAccel

        accelMag = 0.7f * gravityRemoved + 0.3f * derivative
        gyroMag  = sqrt(gxF * gxF + gyF * gyF + gzF * gzF)

        // ── FSM ───────────────────────────────────────────────────────────────
        val now = System.currentTimeMillis()
        when (state) {
            WalkState.STANDING -> {
                if (accelMag > 0.06f) {
                    state = WalkState.TRANSITION
                    walkingTimerMs = now
                }
            }
            WalkState.TRANSITION -> when {
                accelMag > 0.08f && (now - walkingTimerMs > 200L) ->
                    state = WalkState.WALKING
                accelMag < 0.03f ->
                    state = WalkState.STANDING
            }
            WalkState.WALKING -> {
                if (accelMag < 0.025f) {
                    if (standingTimerMs == 0L) standingTimerMs = now
                    if (now - standingTimerMs > 1200L) {
                        state           = WalkState.STANDING
                        standingTimerMs = 0L
                    }
                } else {
                    standingTimerMs = 0L
                }
            }
        }

        // ── Step detection ────────────────────────────────────────────────────
        if (state == WalkState.WALKING) {
            if (accelMag > stepThreshold && !stepTriggered) {
                stepTriggered = true
                stepCount++
            }
            if (accelMag < 0.03f) stepTriggered = false
        } else {
            stepTriggered = false
        }

        return stepCount
    }

    fun reset() {
        stepCount          = 0
        stepTriggered      = false
        state              = WalkState.STANDING
        filterInit         = false
        previousTotalAccel = 1.0f
        standingTimerMs    = 0L
        walkingTimerMs     = 0L
    }
}

// ════════════════════════════════════════════════════════════════════════════
// DUAL-FOOT STEP AGGREGATOR
//
// BleViewModel already parses the raw BLE string into SensorPacket via
// SensorPacket.fromBleString(raw).  It then calls processLeft(packet) or
// processRight(packet) directly — no re-parsing needed here.
// ════════════════════════════════════════════════════════════════════════════

class DualFootStepCounter {

    val leftFSM  = WalkingFSM()
    val rightFSM = WalkingFSM()

    /** Combined total = left steps + right steps. */
    val totalSteps: Int
        get() = leftFSM.stepCount + rightFSM.stepCount

    /** Feed a pre-parsed packet from the LEFT insole. Returns updated total. */
    fun processLeft(packet: SensorPacket): Int {
        leftFSM.process(packet)
        return totalSteps
    }

    /** Feed a pre-parsed packet from the RIGHT insole. Returns updated total. */
    fun processRight(packet: SensorPacket): Int {
        rightFSM.process(packet)
        return totalSteps
    }

    /** Convenience helper to process raw string. */
    fun processBleString(raw: String, isLeft: Boolean): Int? {
        val packet = SensorPacket.fromBleString(raw) ?: return null
        return if (isLeft) processLeft(packet) else processRight(packet)
    }

    /** WALKING > TRANSITION > STANDING — whichever foot is more active wins. */
    val dominantState: WalkState
        get() {
            val l = leftFSM.state
            val r = rightFSM.state
            return when {
                l == WalkState.WALKING    || r == WalkState.WALKING    -> WalkState.WALKING
                l == WalkState.TRANSITION || r == WalkState.TRANSITION -> WalkState.TRANSITION
                else                                                    -> WalkState.STANDING
            }
        }

    /** Average accelMag of both feet — used for the bar chart. */
    val combinedAccelMag: Float
        get() = (leftFSM.accelMag + rightFSM.accelMag) / 2f

    fun reset() {
        leftFSM.reset()
        rightFSM.reset()
    }
}

// ════════════════════════════════════════════════════════════════════════════
// COMPOSABLE CARD
//
// BleViewModel exposes:
//   val bleState: StateFlow<BleUiState>   (totalSteps, walkState, leftRawData…)
//
// Usage:
//   val state by viewModel.bleState.collectAsState()
//   StepsMetricCard(
//       stepCount     = state.totalSteps,
//       walkState     = state.walkState,
//       leftConnected = state.isLeftConnected,
//       rightConnected= state.isRightConnected,
//       stepGoal      = 10_000,
//   )
//
// The card is now display-only — all FSM logic lives in BleViewModel.
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun StepsMetricCard(
    stepCount: Int,
    walkState: WalkState,
    leftConnected: Boolean,
    rightConnected: Boolean,
    rawBleLeft: String?,
    rawBleRight: String?,
    stepGoal: Int = 10_000,
    leftPacketSeq: Long = 0L,
    rightPacketSeq: Long = 0L,
    modifier: Modifier = Modifier,
) {
    // ── Rolling bar chart (kept in composable — it's purely display state) ───
    val barWindow = remember {
        ArrayDeque<Float>(14).also { dq -> repeat(14) { dq.add(0.01f) } }
    }
    val localCounter = remember { DualFootStepCounter() }

    // Refreshes the bar whenever a new left or right packet arrives
    LaunchedEffect(leftPacketSeq, rightPacketSeq) {
        if (!rawBleLeft.isNullOrBlank()) {
            localCounter.processBleString(rawBleLeft, isLeft = true)
        }
        if (!rawBleRight.isNullOrBlank()) {
            localCounter.processBleString(rawBleRight, isLeft = false)
        }
        barWindow.removeFirst()
        barWindow.addLast(localCounter.combinedAccelMag.coerceAtLeast(0.01f))
    }

    val barHeights = barWindow.toList()
    val maxBar     = barHeights.max().coerceAtLeast(0.2f)

    // ─────────────────────────────────────────────────────────────────────────
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

            // ── Header ────────────────────────────────────────────────────────
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
                    ConnectionDot(connected = leftConnected,  label = "L")
                    ConnectionDot(connected = rightConnected, label = "R")
                }
                WalkStateBadge(state = walkState)
            }

            // ── Circular progress ─────────────────────────────────────────────
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

            // ── Bar chart ─────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                barHeights.forEach { h -> MiniBar(heightFraction = h / maxBar) }
            }

            // ── Time axis ─────────────────────────────────────────────────────
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

// ════════════════════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

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
