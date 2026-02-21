package com.android.gridsdk.library.model

import org.junit.Assert.*
import org.junit.Test

class GridSizeTest {

    @Test
    fun `GridSize with valid dimensions creates successfully`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertEquals(5, gridSize.rows)
        assertEquals(4, gridSize.columns)
        assertEquals(20, gridSize.totalCells)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridSize with zero rows throws exception`() {
        GridSize(rows = 0, columns = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridSize with negative rows throws exception`() {
        GridSize(rows = -1, columns = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridSize with zero columns throws exception`() {
        GridSize(rows = 5, columns = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridSize with negative columns throws exception`() {
        GridSize(rows = 5, columns = -1)
    }

    @Test
    fun `isValidPosition returns true for valid positions`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertTrue(gridSize.isValidPosition(0, 0))
        assertTrue(gridSize.isValidPosition(4, 3))
        assertTrue(gridSize.isValidPosition(2, 2))
    }

    @Test
    fun `isValidPosition returns false for invalid positions`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertFalse(gridSize.isValidPosition(-1, 0))
        assertFalse(gridSize.isValidPosition(0, -1))
        assertFalse(gridSize.isValidPosition(5, 0))
        assertFalse(gridSize.isValidPosition(0, 4))
        assertFalse(gridSize.isValidPosition(5, 4))
    }

    @Test
    fun `isWithinBounds returns true for valid bounds`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertTrue(gridSize.isWithinBounds(0, 0, 1, 1))
        assertTrue(gridSize.isWithinBounds(0, 0, 5, 4))
        assertTrue(gridSize.isWithinBounds(3, 2, 2, 2))
        assertTrue(gridSize.isWithinBounds(4, 3, 1, 1))
    }

    @Test
    fun `isWithinBounds returns false for out of bounds`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertFalse(gridSize.isWithinBounds(-1, 0, 1, 1))
        assertFalse(gridSize.isWithinBounds(0, -1, 1, 1))
        assertFalse(gridSize.isWithinBounds(0, 0, 6, 4))
        assertFalse(gridSize.isWithinBounds(0, 0, 5, 5))
        assertFalse(gridSize.isWithinBounds(4, 3, 2, 2))
    }

    @Test
    fun `totalCells calculates correctly`() {
        assertEquals(20, GridSize(5, 4).totalCells)
        assertEquals(16, GridSize(4, 4).totalCells)
        assertEquals(1, GridSize(1, 1).totalCells)
        assertEquals(100, GridSize(10, 10).totalCells)
    }

    @Test
    fun `default grid size constants are valid`() {
        assertEquals(4, GridSize.DEFAULT.rows)
        assertEquals(4, GridSize.DEFAULT.columns)
        assertEquals(16, GridSize.DEFAULT.totalCells)

        assertEquals(5, GridSize.LAUNCHER_STANDARD.rows)
        assertEquals(4, GridSize.LAUNCHER_STANDARD.columns)
        assertEquals(20, GridSize.LAUNCHER_STANDARD.totalCells)
    }
}
