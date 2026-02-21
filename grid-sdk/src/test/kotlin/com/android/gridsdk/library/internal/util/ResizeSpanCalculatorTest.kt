package com.android.gridsdk.library.internal.util

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class ResizeSpanCalculatorTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `clampSpanForItem enforces minimum of 1`() {
        val item = GridItem("a", 0, 0, 2, 2)
        val (x, y) = ResizeSpanCalculator.clampSpanForItem(item, 0, -1, gridSize)
        assertEquals(1, x)
        assertEquals(1, y)
    }

    @Test
    fun `clampSpanForItem enforces maximum within grid`() {
        val item = GridItem("a", 2, 2, 1, 1)
        val (x, y) = ResizeSpanCalculator.clampSpanForItem(item, 10, 10, gridSize)
        assertEquals(2, x)
        assertEquals(2, y)
    }

    @Test
    fun `clampSpanForItem allows valid span`() {
        val item = GridItem("a", 0, 0, 1, 1)
        val (x, y) = ResizeSpanCalculator.clampSpanForItem(item, 2, 3, gridSize)
        assertEquals(2, x)
        assertEquals(3, y)
    }

    @Test
    fun `computeSpanFromDelta adds delta and clamps`() {
        val item = GridItem("a", 0, 0, 2, 2)
        val (x, y) = ResizeSpanCalculator.computeSpanFromDelta(item, 1, -1, gridSize)
        assertEquals(3, x)
        assertEquals(1, y)
    }

    @Test
    fun `computeSpanFromDragEnd computes span from drag end cell`() {
        val item = GridItem("a", 0, 0, 1, 1)
        val (x, y) = ResizeSpanCalculator.computeSpanFromDragEnd(item, 2, 3, gridSize)
        assertEquals(3, x)
        assertEquals(4, y)
    }

    @Test
    fun `computeSpanWithHysteresis keeps previous when delta is small`() {
        val item = GridItem("a", 0, 0, 2, 2)
        val (x, y) = ResizeSpanCalculator.computeSpanWithHysteresis(
            item, 2, 2, 2, 2, gridSize, hysteresisCells = 1
        )
        assertEquals(2, x)
        assertEquals(2, y)
    }

    @Test
    fun `computeSpanWithHysteresis updates when delta exceeds threshold`() {
        val item = GridItem("a", 0, 0, 2, 2)
        val (x, y) = ResizeSpanCalculator.computeSpanWithHysteresis(
            item, 3, 3, 2, 2, gridSize, hysteresisCells = 1
        )
        assertEquals(3, x)
        assertEquals(3, y)
    }

    @Test
    fun `pixelsToCellDelta converts correctly`() {
        assertEquals(2, ResizeSpanCalculator.pixelsToCellDelta(100f, 50f))
        assertEquals(0, ResizeSpanCalculator.pixelsToCellDelta(0f, 50f))
        assertEquals(0, ResizeSpanCalculator.pixelsToCellDelta(10f, 0f))
    }
}
