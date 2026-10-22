package com.project.insole.features.sensor.domain.usecase

import javax.inject.Inject

/**
 * Maps the five plantar FSR readings (kPa, in [com.project.insole.features.sensor.domain.model.FSR_ZONE_ORDER]
 * order) onto a 50-cell grid for heatmap visualization.
 *
 * Grid layout — row-major, 5 rows × 10 columns, ONE GENERIC foot (the
 * renderer mirrors columns for the right foot):
 *  - y = 0 top (toes) → y = 4 bottom (heel)
 *  - x = 0 medial → x = 9 lateral
 *
 * Anatomical placement (p1..p5 per the owner's insole layout figure):
 *  - y = 0: HALLUX owns columns 0–2; columns 3–9 are unsensored lateral toes → 0
 *  - y = 1: MTH1 owns columns 0–3, MTH2 columns 4–6, MTH4 columns 7–9
 *  - y = 2: MIDFOOT — no pressure sensor there (NTC only) → all 0
 *  - y = 3–4: HEEL owns all 10 columns
 *
 * A 1-entry input (legacy v1 single-FSR payload) fills all 50 cells
 * uniformly — un-zoned. An empty input yields all zeros. Pure Kotlin, no
 * Android or BLE dependencies.
 */
class MapPressureToGridUseCase @Inject constructor() {

    operator fun invoke(zonePressuresKpa: List<Float>): List<Float> {
        val grid = FloatArray(GRID_CELLS)

        when {
            zonePressuresKpa.size >= ZONED_INPUT_SIZE -> {
                val hallux = zonePressuresKpa[0]
                val mth1 = zonePressuresKpa[1]
                val mth2 = zonePressuresKpa[2]
                val mth4 = zonePressuresKpa[3]
                val heel = zonePressuresKpa[4]

                // y = 0 — hallux band, medial columns 0–2 only.
                for (x in 0..2) grid[row0 + x] = hallux

                // y = 1 — forefoot band: MTH1 | MTH2 | MTH4.
                for (x in 0..3) grid[GRID_COLS + x] = mth1
                for (x in 4..6) grid[GRID_COLS + x] = mth2
                for (x in 7..9) grid[GRID_COLS + x] = mth4

                // y = 2 — midfoot: stays 0 (no pressure sensor; NTC only).

                // y = 3–4 — heel owns the full width.
                for (y in 3 until GRID_ROWS) {
                    for (x in 0 until GRID_COLS) {
                        grid[y * GRID_COLS + x] = heel
                    }
                }
            }

            zonePressuresKpa.size == 1 -> {
                // Legacy v1 payload — one FSR under the whole foot, no zone
                // info: uniform fill so the UI can render an honest "single
                // sensor" footprint.
                val value = zonePressuresKpa[0]
                for (i in 0 until GRID_CELLS) grid[i] = value
            }

            // Empty (or an unrecognized partial payload) → all zeros.
        }

        return grid.toList()
    }

    companion object {
        const val GRID_COLS = 10
        const val GRID_ROWS = 5
        const val GRID_CELLS = GRID_ROWS * GRID_COLS

        /** Input size that marks a payload as the zoned v2 stream (p1..p5). */
        const val ZONED_INPUT_SIZE = 5

        private const val row0 = 0
    }
}
