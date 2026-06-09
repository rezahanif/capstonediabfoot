package com.project.insole.features.sensor.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.insole.core.theme.DashboardColors
import kotlin.math.min

// ════════════════════════════════════════════════════════════════════════════
// SENSOR ZONE DEFINITIONS
//
// Each insole has 5 FSR sensor positions mapped to anatomical landmarks:
//
//   Zone 0 — Heel           (1 sensor, centre-rear calcaneus)
//   Zone 1 — Metatarsal 1   (1 sensor, 1st met-head, medial ball)
//   Zone 2 — Metatarsal 2   (1 sensor, 2nd met-head, central ball)
//   Zone 3 — Metatarsal 5   (1 sensor, 5th met-head, lateral ball)
//   Zone 4 — Hallux         (1 sensor, 1st toe / big-toe pad)
//
// Positions are expressed as fractions of the foot bounding box
// (0,0) = top-left of the foot silhouette, (1,1) = bottom-right.
//
// ─── SINGLE-SENSOR MODE (current hardware) ───────────────────────────────
// You have 1 sensor per insole now.  We distribute that single value across
// all 5 zones using high-pressure anatomical weight-bearing ratios from
// normal gait literature:
//   Heel ~35 %  (dominant heel-strike load)
//   Met-1 ~25 % (primary push-off / medial ball)
//   Met-2 ~20 % (secondary central ball load)
//   Met-5 ~12 % (lateral ball, lower but significant)
//   Hallux ~8 % (toe-off contribution)
// Weights sum to 1.0.  When you add the other 4 sensors, swap
// distributeFromSingle() for a direct mapping.
// ════════════════════════════════════════════════════════════════════════════

private data class SensorZone(
    /** Fractional X/Y position within the foot bounding box (0..1). */
    val xFrac: Float,
    val yFrac: Float,
    /** Radius of the heatmap blob as a fraction of the foot WIDTH. */
    val radiusFrac: Float,
    /** Default weight when running in single-sensor mode. */
    val defaultWeight: Float,
)

/**
 * Anatomical sensor positions for a LEFT foot.
 * Right foot zones are mirrored horizontally inside [FootHeatmap].
 *
 * Y=0 is the TOE end, Y=1 is the HEEL end  (matching the foot silhouette
 * path which is drawn top=toes, bottom=heel).
 *
 * Zone layout (left foot, looking at plantar surface):
 *   0 — Heel        : centre-rear calcaneus
 *   1 — Metatarsal 1: medial ball (1st met-head), inner forefoot
 *   2 — Metatarsal 2: central ball (2nd met-head), mid forefoot
 *   3 — Metatarsal 5: lateral ball (5th met-head), outer forefoot
 *   4 — Hallux      : big-toe pad, slightly medial
 */
private val LEFT_ZONES = listOf(
    SensorZone(xFrac = 0.50f, yFrac = 0.88f, radiusFrac = 0.32f, defaultWeight = 0.35f), // 0 Heel
    SensorZone(xFrac = 0.28f, yFrac = 0.40f, radiusFrac = 0.22f, defaultWeight = 0.25f), // 1 Metatarsal 1
    SensorZone(xFrac = 0.45f, yFrac = 0.38f, radiusFrac = 0.20f, defaultWeight = 0.20f), // 2 Metatarsal 2
    SensorZone(xFrac = 0.70f, yFrac = 0.36f, radiusFrac = 0.18f, defaultWeight = 0.12f), // 3 Metatarsal 5
    SensorZone(xFrac = 0.28f, yFrac = 0.10f, radiusFrac = 0.20f, defaultWeight = 0.08f), // 4 Hallux
)

/**
 * Convert a single FSR reading (0..255) into per-zone intensity values
 * using high-pressure anatomical weight-bearing ratios.
 *
 * Weights (must sum to 1.0):
 *   Heel 35 % · Met-1 25 % · Met-2 20 % · Met-5 12 % · Hallux 8 %
 *
 * Each zone intensity = normalised_value × (zone_weight / max_weight),
 * so the dominant zone (Heel) always reaches full intensity at peak load,
 * and lighter zones scale proportionally — preserving the high-pressure
 * distribution hierarchy across the single-sensor prototype.
 *
 * When you upgrade to 5 sensors, replace this with:
 *   fun directMapping(values: List<Float>) = values   (one-to-one)
 */
private fun distributeFromSingle(rawValue: Float): List<Float> {
    val normalised = (rawValue / 255f).coerceIn(0f, 1f)
    return LEFT_ZONES.map { zone -> normalised * zone.defaultWeight * (1f / LEFT_ZONES.maxOf { it.defaultWeight }) }
}

// ════════════════════════════════════════════════════════════════════════════
// HEATMAP COLOUR SCALE  (blue → cyan → green → yellow → red)
// Matches the legend in the reference design exactly.
// ════════════════════════════════════════════════════════════════════════════

private val HEATMAP_COLORS = listOf(
    Color(0xFF0A1172), // 0.00 — deep blue  (no pressure)
    Color(0xFF0066CC), // 0.15
    Color(0xFF00AAFF), // 0.30 — cyan
    Color(0xFF00DDAA), // 0.45 — teal
    Color(0xFF44FF44), // 0.60 — green
    Color(0xFFFFFF00), // 0.75 — yellow
    Color(0xFFFF8800), // 0.88 — orange
    Color(0xFFFF2200), // 1.00 — red        (peak pressure)
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
// SINGLE FOOT HEATMAP  (Canvas composable)
// ════════════════════════════════════════════════════════════════════════════

/**
 * Draws one foot with a pressure heatmap overlay.
 *
 * @param zoneIntensities  List of 5 floats in [0..1], one per sensor zone.
 *                         Index order matches [LEFT_ZONES].
 * @param mirrored         true = right foot (reflects X axis).
 * @param connected        false = BLE device not connected; shows a greyed-out
 *                         silhouette with a dashed border and a "No Signal" label.
 */
@Composable
private fun FootHeatmap(
    zoneIntensities: List<Float>,
    mirrored: Boolean,
    connected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {

        // ── Heatmap canvas (always drawn; faded when disconnected) ────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (connected) 1f else 0.25f),
        ) {
            val w = size.width
            val h = size.height

            val footPath = buildFootPath(w, h, mirrored)

            // ── 1. Dark blue base fill ──────────────────────────────────────────
            clipPath(footPath) {
                drawPath(footPath, color = Color(0xFF0A2050))

                // ── 2. Per-zone radial gradient blobs ──────────────────────────
                LEFT_ZONES.forEachIndexed { i, zone ->
                    val intensity = zoneIntensities.getOrElse(i) { 0f }
                    if (intensity < 0.02f) return@forEachIndexed   // skip invisible

                    // Mirror the X position for the right foot
                    val zoneX = if (mirrored) w * (1f - zone.xFrac) else w * zone.xFrac
                    val zoneY = h * zone.yFrac
                    val radius = min(w, h) * zone.radiusFrac

                    // Outer soft glow (full radius, low alpha)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to heatColor(intensity).copy(alpha = intensity * 0.55f),
                                0.6f to heatColor(intensity * 0.6f).copy(alpha = intensity * 0.25f),
                                1.0f to Color.Transparent,
                            ),
                            center = Offset(zoneX, zoneY),
                            radius = radius * 1.4f,
                        ),
                        radius = radius * 1.4f,
                        center = Offset(zoneX, zoneY),
                    )

                    // Inner hot core (tighter, brighter)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to heatColor(intensity).copy(alpha = intensity * 0.90f),
                                0.5f to heatColor(intensity * 0.7f).copy(alpha = intensity * 0.50f),
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

            // ── 3. Foot outline stroke (on top of clip, so it stays crisp) ─────
            drawPath(
                path   = footPath,
                color  = Color.White.copy(alpha = 0.30f),
                style  = Stroke(width = 2.5f),
            )

            // ── 4. Dashed border outline when disconnected ──────────────────────
            if (!connected) {
                drawPath(
                    path  = footPath,
                    color = Color(0xFFAAAAAA).copy(alpha = 0.60f),
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
                    tint               = Color(0xFF888888),
                    modifier           = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text        = "No Signal",
                    color       = Color(0xFF888888),
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
            color      = Color(0xFFAAAAAA),
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
            color      = Color(0xFFAAAAAA),
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
// rawBleLeft   Latest BLE string from Smart_Insole_Left  (or null)
// rawBleRight  Latest BLE string from Smart_Insole_Right (or null)
//
// The pressure value is the 7th comma-separated field (index 6, 0-based)
// in the BLE packet: "ax,ay,az,gx,gy,gz,pressureKPa,tempCelsius"
// Range: 0–255 (mapped from the RFP ADC by the firmware).
//
// ── Upgrading to 5 sensors ────────────────────────────────────────────────
// When your hardware has 5 FSR sensors, change the BLE packet to send all
// 5 values (e.g. append "p0,p1,p2,p3,p4" after tempCelsius) and replace
// the call to distributeFromSingle() with a direct 1-to-1 mapping:
//
//   // Zone order: Heel, Met-1, Met-2, Met-5, Hallux
//   val zones = listOf(p0/255f, p1/255f, p2/255f, p3/255f, p4/255f)
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun PlantarPressureCard(
    rawBleLeft: String?,
    rawBleRight: String?,
    leftConnected: Boolean,
    rightConnected: Boolean,
    leftPacketSeq: Long = 0L,
    rightPacketSeq: Long = 0L,
    leftPeakPressure: Float = 0f,
    rightPeakPressure: Float = 0f,
    modifier: Modifier = Modifier,
) {
    // ── Connection state — each foot is independent ───────────────────────────
    val anyConnected = leftConnected || rightConnected

    // ── Parse pressure from BLE strings ──────────────────────────────────────
    // Use seq as key so it recomputes every new packet regardless of content equality
    val leftPressure = remember(leftPacketSeq) { parsePressure(rawBleLeft) }
    val rightPressure = remember(rightPacketSeq) { parsePressure(rawBleRight) }

    // ── Distribute single sensor → 5 zone intensities ────────────────────────
    val leftZones = remember(leftPacketSeq) { distributeFromSingle(leftPressure) }
    val rightZones = remember(rightPacketSeq) { distributeFromSingle(rightPressure) }

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
                        color = Color(0xFFEEEEEE),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.BluetoothDisabled,
                                contentDescription = null,
                                tint               = Color(0xFF999999),
                                modifier           = Modifier.size(12.dp),
                            )
                            Text(
                                text       = "OFFLINE",
                                color      = Color(0xFF999999),
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
                        zoneIntensities = leftZones,
                        mirrored        = false,
                        connected       = leftConnected,
                        modifier        = Modifier
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
                                    else Color(0xFFCCCCCC),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                )
                        )
                        Text(
                            text       = "Left",
                            color      = if (leftConnected) Color(0xFF555555) else Color(0xFFAAAAAA),
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
                        zoneIntensities = rightZones,
                        mirrored        = true,
                        connected       = rightConnected,
                        modifier        = Modifier
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
                                    else Color(0xFFCCCCCC),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                )
                        )
                        Text(
                            text       = "Right",
                            color      = if (rightConnected) Color(0xFF555555) else Color(0xFFAAAAAA),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
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

/** Extract the pressure value (field index 6) from a raw BLE string. */
private fun parsePressure(raw: String?): Float {
    if (raw.isNullOrBlank()) return 0f
    return try {
        raw.trim().split(",").getOrNull(6)?.toFloat() ?: 0f
    } catch (_: Exception) { 0f }
}

@Composable
private fun PressureReadout(label: String, value: Float, connected: Boolean) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text       = label,
            color      = Color(0xFFAAAAAA),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text       = if (connected) "%.0f".format(value) else "– –",
            color      = if (connected) DashboardColors.Navy else Color(0xFFCCCCCC),
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
