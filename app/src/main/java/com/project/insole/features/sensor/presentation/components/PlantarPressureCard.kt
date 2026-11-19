package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BluetoothDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import com.project.insole.features.sensor.domain.usecase.MapPressureToGridUseCase
import com.project.insole.features.sensor.presentation.PressureMapState

// ════════════════════════════════════════════════════════════════════════════
// PLANTAR PRESSURE MAP — real data only
//
// The heatmap renders the 50-cell grid produced by MapPressureToGridUseCase
// from the insole's per-zone FSR readings (SensorPacketParser →
// BleSensorDataSource → SensorViewModel.pressureMap). No raw-string parsing
// and no fabricated zone distribution happens here anymore.
//
// Intensity is RELATIVE across both feet (cell ÷ max cell of both grids):
// there is no calibrated absolute pressure scale yet — TD-11. Legacy v1
// firmware sends a single whole-foot FSR, so the grid arrives uniform and is
// scaled by the POC raw range (0–255); the card then says so in a caption.
// ════════════════════════════════════════════════════════════════════════════

// ─── HEATMAP COLOUR SCALE  (blue → cyan → green → yellow → red) ─────────────
// Matches the legend in the reference design exactly.

private val HEATMAP_COLORS = listOf(
    DashboardColors.Heatmap00, // 0.00 — deep blue  (no pressure)
    DashboardColors.Heatmap15, // 0.15
    DashboardColors.Heatmap30, // 0.30 — cyan
    DashboardColors.Heatmap45, // 0.45 — teal
    DashboardColors.Heatmap60, // 0.60 — green
    DashboardColors.Heatmap75, // 0.75 — yellow
    DashboardColors.Heatmap88, // 0.88 — orange
    DashboardColors.Heatmap100 // 1.00 — red        (peak pressure)
)

private fun heatColor(intensity: Float): Color {
    val t = intensity.coerceIn(0f, 1f)
    val scaled = t * (HEATMAP_COLORS.size - 1)
    val lo = scaled.toInt().coerceIn(0, HEATMAP_COLORS.size - 2)
    val hi = lo + 1
    val frac = scaled - lo
    val a = HEATMAP_COLORS[lo]
    val b = HEATMAP_COLORS[hi]
    return Color(
        red   = a.red   + (b.red   - a.red)   * frac,
        green = a.green + (b.green - a.green) * frac,
        blue  = a.blue  + (b.blue  - a.blue)  * frac,
        alpha = 0.85f,
    )
}

// ════════════════════════════════════════════════════════════════════════════
// FOOT SILHOUETTE PATH
//
// A smooth foot outline built from cubic Bézier curves.
// The path occupies the full canvas size passed in; it is normalised so
// x ∈ [0, width], y ∈ [0, height] with (0,0) at the TOP (toe end).
//
// mirrored = true  → right foot  (reflected on the X axis)
// ════════════════════════════════════════════════════════════════════════════

private fun buildFootPath(w: Float, h: Float, mirrored: Boolean): Path {
    // All control points expressed as fractions of w / h
    // then optionally mirrored.
    fun x(frac: Float) = if (mirrored) w * (1f - frac) else w * frac
    fun y(frac: Float) = h * frac

    return Path().apply {
        // ── Start: inner side of big toe ──────────────────────────────────
        moveTo(x(0.38f), y(0.04f))

        // Toe mound (big → pinky)
        cubicTo(
            x(0.55f), y(0.00f),
            x(0.75f), y(0.02f),
            x(0.80f), y(0.10f),
        )
        // Outer (pinky) side curving down
        cubicTo(
            x(0.88f), y(0.18f),
            x(0.90f), y(0.30f),
            x(0.87f), y(0.44f),
        )
        // Ball of foot (outer)
        cubicTo(
            x(0.86f), y(0.52f),
            x(0.82f), y(0.56f),
            x(0.78f), y(0.60f),
        )
        // Lateral mid-arch (narrow waist)
        cubicTo(
            x(0.76f), y(0.66f),
            x(0.72f), y(0.72f),
            x(0.74f), y(0.80f),
        )
        // Heel (outer curve)
        cubicTo(
            x(0.76f), y(0.90f),
            x(0.68f), y(1.00f),
            x(0.52f), y(1.00f),
        )
        // Heel (inner curve)
        cubicTo(
            x(0.34f), y(1.00f),
            x(0.22f), y(0.92f),
            x(0.24f), y(0.82f),
        )
        // Medial arch (inner narrow waist)
        cubicTo(
            x(0.26f), y(0.70f),
            x(0.20f), y(0.60f),
            x(0.18f), y(0.52f),
        )
        // Ball of foot (inner / medial)
        cubicTo(
            x(0.14f), y(0.44f),
            x(0.16f), y(0.34f),
            x(0.20f), y(0.26f),
        )
        // Inner side rising to big toe
        cubicTo(
            x(0.24f), y(0.16f),
            x(0.30f), y(0.08f),
            x(0.38f), y(0.04f),
        )

        close()
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SENSOR ZONE DEFINITIONS
// ════════════════════════════════════════════════════════════════════════════

private data class SensorZone(
    val xFrac: Float,
    val yFrac: Float,
    val radiusFrac: Float,
    val defaultWeight: Float = 0.2f,
)

/**
 * Anatomical sensor positions for a LEFT foot, matching [com.project.insole.features.sensor.domain.model.FSR_ZONE_ORDER]:
 *   0 — Hallux        : big-toe pad (medial forefoot, top)
 *   1 — Metatarsal 1  : 1st met-head (medial ball)
 *   2 — Metatarsal 2  : 2nd met-head (central ball)
 *   3 — Metatarsal 4  : 4th met-head (lateral ball)
 *   4 — Heel          : calcaneus (centre-rear)
 */
private val ANATOMICAL_ZONES = listOf(
    SensorZone(xFrac = 0.28f, yFrac = 0.10f, radiusFrac = 0.20f, defaultWeight = 0.10f), // 0 Hallux
    SensorZone(xFrac = 0.28f, yFrac = 0.38f, radiusFrac = 0.22f, defaultWeight = 0.25f), // 1 Metatarsal 1
    SensorZone(xFrac = 0.46f, yFrac = 0.36f, radiusFrac = 0.20f, defaultWeight = 0.20f), // 2 Metatarsal 2
    SensorZone(xFrac = 0.70f, yFrac = 0.36f, radiusFrac = 0.18f, defaultWeight = 0.15f), // 3 Metatarsal 4
    SensorZone(xFrac = 0.50f, yFrac = 0.86f, radiusFrac = 0.30f, defaultWeight = 0.30f), // 4 Heel
)

// ════════════════════════════════════════════════════════════════════════════
// SINGLE FOOT HEATMAP  (Canvas composable)
// ════════════════════════════════════════════════════════════════════════════

/**
 * Draws one foot with continuous, natural radial gradient heat clouds
 * mapped to anatomical sensor zones, clipped to the foot silhouette.
 */
@Composable
private fun FootHeatmap(
    zonePressures: List<Float> = emptyList(),
    grid: List<Float> = emptyList(),
    mirrored: Boolean,
    maxIntensity: Float?,
    isZoned: Boolean,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {

        // ── Heatmap canvas ────────────────────────────────────────────────────
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val w = size.width
            val h = size.height

            val footPath = buildFootPath(w, h, mirrored)

            if (connected) {
                // Resolve the 5 zone readings from direct list or fallback 50-cell grid
                val resolvedZonePressures: List<Float> = when {
                    zonePressures.size >= 5 -> zonePressures
                    grid.size >= 50 -> listOf(
                        grid[0],  // Hallux
                        grid[10], // MTH1
                        grid[14], // MTH2
                        grid[17], // MTH4
                        grid[30], // Heel
                    )
                    zonePressures.isNotEmpty() -> zonePressures
                    grid.isNotEmpty() -> listOf(grid[0])
                    else -> emptyList()
                }

                // Count active FSR sensors (threshold > 1.0f kPa to filter continuous noise / firmware drift)
                val activeSensorsCount = resolvedZonePressures.take(5).count { it > 1.0f }
                // Only display heat gradient when at least 3 of the 5 sensors are actively sending data;
                // otherwise maintain the clean connected "idle blue" state without heatmap overlay.
                val isActivelyStepping = activeSensorsCount >= 3

                // ── 1. Dark blue base fill for connected insole ("idle blue") ────────
                clipPath(footPath) {
                    drawPath(footPath, color = DashboardColors.FootSilhouette)

                    // ── 2. Per-zone smooth radial gradient heat clouds (only when >= 3 sensors active) ───
                    if (isActivelyStepping) {
                        val intensities: List<Float> = if (isZoned) {
                            if (maxIntensity == null || maxIntensity <= 0f) {
                                List(5) { 0f }
                            } else {
                                resolvedZonePressures.map { (it / maxIntensity).coerceIn(0f, 1f) }
                            }
                        } else {
                            val singleVal = resolvedZonePressures.firstOrNull() ?: 0f
                            val norm = (singleVal / 255f).coerceIn(0f, 1f)
                            ANATOMICAL_ZONES.map { zone ->
                                (norm * zone.defaultWeight * (1f / 0.30f)).coerceIn(0f, 1f)
                            }
                        }

                        ANATOMICAL_ZONES.forEachIndexed { i, zone ->
                            val intensity = intensities.getOrElse(i) { 0f }
                            if (intensity < 0.02f) return@forEachIndexed

                            // Mirror flip mapper position horizontally along Y-axis so medial pads stay on the inner side
                            val zoneX = if (mirrored) w * zone.xFrac else w * (1f - zone.xFrac)
                            val zoneY = h * zone.yFrac
                            val radius = kotlin.math.min(w, h) * zone.radiusFrac

                            // Outer soft glow (wide spread, low opacity)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.0f to heatColor(intensity).copy(alpha = intensity * 0.60f),
                                        0.5f to heatColor(intensity * 0.6f).copy(alpha = intensity * 0.30f),
                                        1.0f to Color.Transparent,
                                    ),
                                    center = Offset(zoneX, zoneY),
                                    radius = radius * 1.5f,
                                ),
                                radius = radius * 1.5f,
                                center = Offset(zoneX, zoneY),
                            )

                            // Inner hot core (saturated, bright center)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.0f to heatColor(intensity).copy(alpha = (intensity * 0.95f).coerceAtMost(1f)),
                                        0.6f to heatColor(intensity * 0.8f).copy(alpha = (intensity * 0.60f).coerceAtMost(1f)),
                                        1.0f to Color.Transparent,
                                    ),
                                    center = Offset(zoneX, zoneY),
                                    radius = radius,
                                ),
                                radius = radius,
                                center = Offset(zoneX, zoneY),
                            )
                        }
                    }
                }

                // ── 3. Foot outline stroke (on top of clip, so it stays crisp) ─────
                drawPath(
                    path   = footPath,
                    color  = Color.White.copy(alpha = 0.30f),
                    style  = Stroke(width = 2.5f),
                )
            } else {
                // ── Disconnected / No Signal: Honest neutral grey silhouette ─────────
                clipPath(footPath) {
                    drawPath(footPath, color = DashboardColors.ProgressTrack)
                }

                // Dashed border outline when disconnected
                drawPath(
                    path  = footPath,
                    color = DashboardColors.FootOutlineMuted.copy(alpha = 0.60f),
                    style = Stroke(
                        width      = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                    ),
                )
            }
        } // end Canvas

        // ── Disconnected overlay: icon + label centred on the foot ───────────
        if (!connected) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BluetoothDisabled,
                    contentDescription = "Not connected",
                    tint               = DashboardColors.TextGray,
                    modifier           = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text        = "No Signal",
                    color       = DashboardColors.TextGray,
                    fontSize    = 10.sp,
                    fontWeight  = FontWeight.Medium,
                    textAlign   = TextAlign.Center,
                )
            }
        }
    } // end Box (FootHeatmap)
}

// ════════════════════════════════════════════════════════════════════════════
// COLOUR LEGEND  (vertical bar matching the reference design)
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PressureLegend(modifier: Modifier = Modifier) {
    Column(
        modifier           = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text       = "High",
            color      = DashboardColors.FootOutlineMuted,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
        )

        // Gradient bar
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = HEATMAP_COLORS.reversed(),
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
        )

        Text(
            text       = "Low",
            color      = DashboardColors.FootOutlineMuted,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PUBLIC CARD COMPOSABLE
//
// Parameters
// ──────────
// pressureMap        Pre-rendered 5×10 grids for both feet
//                    (SensorViewModel.pressureMap — computed by
//                    MapPressureToGridUseCase from the parsed v2/legacy
//                    stream). No raw BLE strings are parsed here.
// leftPeakPressure   Session peak total-foot load, shown in the footer.
// rightPeakPressure  Session peak total-foot load, shown in the footer.
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun PlantarPressureCard(
    pressureMap: PressureMapState,
    leftConnected: Boolean,
    rightConnected: Boolean,
    leftPeakPressure: Float = 0f,
    rightPeakPressure: Float = 0f,
    modifier: Modifier = Modifier,
) {
    // ── Connection state — each foot is independent ───────────────────────────
    val anyConnected = leftConnected || rightConnected

    // Relative intensity scale: the strongest reading across actively stepping feet (at least 3 active sensors).
    // null = no pressure anywhere (or nothing connected/stepping) → all cells skip.
    val leftActive = leftConnected && pressureMap.leftZonePressures.take(5).count { it > 1.0f } >= 3
    val rightActive = rightConnected && pressureMap.rightZonePressures.take(5).count { it > 1.0f } >= 3

    val allZones = (if (leftActive) pressureMap.leftZonePressures else emptyList()) +
        (if (rightActive) pressureMap.rightZonePressures else emptyList())
    val maxIntensity = if (pressureMap.isZoned) {
        if (allZones.isNotEmpty()) {
            allZones.maxOrNull()?.takeIf { it > 0f }
        } else {
            val connectedGrids = (if (leftConnected) pressureMap.leftGrid else emptyList()) +
                (if (rightConnected) pressureMap.rightGrid else emptyList())
            connectedGrids.maxOrNull()?.takeIf { it > 0f }
        }
    } else {
        null
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
                .fillMaxWidth(),
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text       = "Plantar Pressure Map",
                    color      = DashboardColors.Navy,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.weight(1f),
                )

                // Badge: REAL-TIME when any foot is live, OFFLINE when both are gone
                if (anyConnected) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = DashboardColors.Green,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        Color.White,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    )
                            )
                            Text(
                                text       = "REAL-TIME",
                                color      = Color.White,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = DashboardColors.ProgressTrack,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.BluetoothDisabled,
                                contentDescription = null,
                                tint               = DashboardColors.TextLightGray,
                                modifier           = Modifier.size(12.dp),
                            )
                            Text(
                                text       = "OFFLINE",
                                color      = DashboardColors.TextLightGray,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Foot heatmaps + legend ────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Left foot
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    FootHeatmap(
                        zonePressures = if (leftConnected) pressureMap.leftZonePressures else emptyList(),
                        grid          = if (leftConnected) pressureMap.leftGrid else emptyList(),
                        mirrored      = false,
                        maxIntensity  = maxIntensity,
                        isZoned       = pressureMap.isZoned,
                        connected     = leftConnected,
                        modifier      = Modifier
                            .fillMaxHeight(0.92f)
                            .fillMaxWidth(),
                    )
                    // Foot label + per-foot connection dot
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (leftConnected) DashboardColors.Green
                                    else DashboardColors.InactiveTrack,
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                )
                        )
                        Text(
                            text       = "Left",
                            color      = if (leftConnected) DashboardColors.TextMuted else DashboardColors.FootOutlineMuted,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // Legend (centre) — dimmed when both disconnected
                PressureLegend(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                        .alpha(if (anyConnected) 1f else 0.3f),
                )

                // Right foot
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    FootHeatmap(
                        zonePressures = if (rightConnected) pressureMap.rightZonePressures else emptyList(),
                        grid          = if (rightConnected) pressureMap.rightGrid else emptyList(),
                        mirrored      = true,
                        maxIntensity  = maxIntensity,
                        isZoned       = pressureMap.isZoned,
                        connected     = rightConnected,
                        modifier      = Modifier
                            .fillMaxHeight(0.92f)
                            .fillMaxWidth(),
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (rightConnected) DashboardColors.Green
                                    else DashboardColors.InactiveTrack,
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                )
                        )
                        Text(
                            text       = "Right",
                            color      = if (rightConnected) DashboardColors.TextMuted else DashboardColors.FootOutlineMuted,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            // ── Legacy-firmware caption: the grid is uniform, not zoned ───────
            if (!pressureMap.isZoned && anyConnected) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "5-zone view requires firmware v2",
                    color      = DashboardColors.TextMuted,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Pressure readout footer ───────────────────────────────────────
            // Shows "– –" instead of a number when the foot is disconnected.
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PressureReadout(
                    label     = "L Peak",
                    value     = leftPeakPressure,
                    connected = leftConnected,
                )
                PressureReadout(
                    label     = "R Peak",
                    value     = rightPeakPressure,
                    connected = rightConnected,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// HELPERS
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PressureReadout(label: String, value: Float, connected: Boolean) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text       = label,
            color      = DashboardColors.FootOutlineMuted,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text       = if (connected) "%.0f".format(value) else "– –",
            color      = if (connected) DashboardColors.Navy else DashboardColors.InactiveTrack,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
