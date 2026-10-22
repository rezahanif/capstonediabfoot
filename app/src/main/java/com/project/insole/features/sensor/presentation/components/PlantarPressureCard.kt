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
 * Draws one foot with the real 50-cell pressure grid clipped to the
 * silhouette.
 *
 * @param grid  Row-major 5×10 pressure grid from [MapPressureToGridUseCase]
 *              (50 [Float]s, one generic foot: x = 0 medial, y = 0 toes).
 * @param mirrored         true = right foot (grid columns are reflected on
 *                         the X axis so medial stays medial).
 * @param maxIntensity     Max cell value across BOTH feet's grids — the
 *                         relative intensity divisor. Only used when
 *                         [isZoned]; null means "no pressure anywhere".
 * @param isZoned          true = v2 five-zone payload (relative kPa scale);
 *                         false = legacy single-FSR payload (uniform grid,
 *                         scaled by the 0–255 POC raw range).
 * @param connected        false = BLE device not connected; shows a greyed-out
 *                         silhouette with a dashed border and a "No Signal" label.
 */
@Composable
private fun FootHeatmap(
    grid: List<Float>,
    mirrored: Boolean,
    maxIntensity: Float?,
    isZoned: Boolean,
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

            val cellW = w / MapPressureToGridUseCase.GRID_COLS
            val cellH = h / MapPressureToGridUseCase.GRID_ROWS

            // ── 1. Dark blue base fill ──────────────────────────────────────────
            clipPath(footPath) {
                drawPath(footPath, color = Color(0xFF0A2050))

                // ── 2. Real 50-cell pressure grid, clipped to the silhouette ────
                // Row-major: row 0 = toe end, row 4 = heel end; column 0 = medial.
                for (row in 0 until MapPressureToGridUseCase.GRID_ROWS) {
                    for (col in 0 until MapPressureToGridUseCase.GRID_COLS) {
                        val value = grid.getOrElse(
                            row * MapPressureToGridUseCase.GRID_COLS + col,
                        ) { 0f }

                        val intensity = if (isZoned) {
                            // Relative scale across both feet — no absolute
                            // scale exists pre-calibration (TD-11).
                            if (maxIntensity == null || maxIntensity <= 0f) 0f
                            else value / maxIntensity
                        } else {
                            // Legacy single-FSR payload in the POC raw range.
                            value / 255f
                        }
                        if (intensity < 0.02f) continue   // skip invisible

                        // Right foot mirrors columns: medial col 0 stays medial.
                        val drawCol = if (mirrored) {
                            MapPressureToGridUseCase.GRID_COLS - 1 - col
                        } else {
                            col
                        }

                        drawRect(
                            color = heatColor(intensity),
                            topLeft = Offset(drawCol * cellW, row * cellH),
                            size = Size(cellW, cellH),
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

    // Relative intensity scale: the strongest cell across BOTH feet's grids.
    // null = no pressure anywhere (or nothing connected) → all cells skip.
    val maxIntensity = if (pressureMap.isZoned) {
        (pressureMap.leftGrid + pressureMap.rightGrid).maxOrNull()?.takeIf { it > 0f }
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
                        grid          = pressureMap.leftGrid,
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
                        grid          = pressureMap.rightGrid,
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
