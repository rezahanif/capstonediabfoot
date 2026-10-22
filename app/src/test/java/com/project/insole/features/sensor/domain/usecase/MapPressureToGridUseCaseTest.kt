package com.project.insole.features.sensor.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the 5×10 plantar pressure grid mapping.
 *
 * Grid: row-major, y = 0 top (toes) → y = 4 bottom (heel), x = 0 medial →
 * x = 9 lateral. Zone input order = FSR_ZONE_ORDER
 * (Hallux, MTH1, MTH2, MTH4, Heel).
 */
class MapPressureToGridUseCaseTest {

    private val useCase = MapPressureToGridUseCase()

    // Distinct value per zone so misplacement is detectable:
    // [0]=Hallux 10, [1]=MTH1 20, [2]=MTH2 30, [3]=MTH4 40, [4]=Heel 50.
    private val zones = listOf(10f, 20f, 30f, 40f, 50f)

    private fun cell(grid: List<Float>, row: Int, col: Int): Float =
        grid[row * MapPressureToGridUseCase.GRID_COLS + col]

    @Test
    fun `grid is always 50 cells`() {
        assertEquals(50, useCase(zones).size)
        assertEquals(50, useCase(emptyList()).size)
        assertEquals(50, useCase(listOf(7f)).size)
    }

    @Test
    fun `hallux value lands in row 0 medial columns`() {
        val grid = useCase(zones)
        assertEquals(10f, cell(grid, 0, 0), 1e-4f)
        assertEquals(10f, cell(grid, 0, 2), 1e-4f)
    }

    @Test
    fun `lateral toe columns in row 0 are unsensored zeros`() {
        val grid = useCase(zones)
        for (col in 3..9) {
            assertEquals(0f, cell(grid, 0, col), 1e-4f)
        }
    }

    @Test
    fun `forefoot bands are placed medial to lateral in row 1`() {
        val grid = useCase(zones)
        assertEquals(20f, cell(grid, 1, 0), 1e-4f)   // MTH1 (cols 0–3)
        assertEquals(20f, cell(grid, 1, 3), 1e-4f)
        assertEquals(30f, cell(grid, 1, 5), 1e-4f)   // MTH2 (cols 4–6)
        assertEquals(40f, cell(grid, 1, 9), 1e-4f)   // MTH4 (cols 7–9)
    }

    @Test
    fun `midfoot row is all zeros - no pressure sensor there`() {
        val grid = useCase(zones)
        for (col in 0 until MapPressureToGridUseCase.GRID_COLS) {
            assertEquals(0f, cell(grid, 2, col), 1e-4f)
        }
    }

    @Test
    fun `heel owns the full width of rows 3 and 4`() {
        val grid = useCase(zones)
        assertEquals(50f, cell(grid, 3, 0), 1e-4f)
        assertEquals(50f, cell(grid, 3, 9), 1e-4f)
        assertEquals(50f, cell(grid, 4, 0), 1e-4f)
        assertEquals(50f, cell(grid, 4, 9), 1e-4f)
    }

    @Test
    fun `single-entry legacy input fills all 50 cells uniformly`() {
        val grid = useCase(listOf(7f))
        assertTrue(grid.all { it == 7f })
    }

    @Test
    fun `empty input yields all zeros`() {
        val grid = useCase(emptyList())
        assertTrue(grid.all { it == 0f })
    }

    @Test
    fun `partial payload below 5 zones yields all zeros`() {
        // 2–4 entries is neither a zoned v2 payload nor a legacy single-FSR
        // one — no zone can be attributed honestly, so the grid stays empty.
        val grid = useCase(listOf(1f, 2f, 3f))
        assertTrue(grid.all { it == 0f })
    }
}
