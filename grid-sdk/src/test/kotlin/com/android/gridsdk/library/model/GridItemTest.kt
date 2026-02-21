package com.android.gridsdk.library.model

import org.junit.Assert.*
import org.junit.Test

class GridItemTest {

    @Test
    fun `GridItem with valid parameters creates successfully`() {
        val item = GridItem(
            id = "item1",
            x = 0,
            y = 0,
            spanX = 2,
            spanY = 3
        )

        assertEquals("item1", item.id)
        assertEquals(0, item.x)
        assertEquals(0, item.y)
        assertEquals(2, item.spanX)
        assertEquals(3, item.spanY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridItem with zero spanX throws exception`() {
        GridItem("item1", 0, 0, spanX = 0, spanY = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridItem with negative spanX throws exception`() {
        GridItem("item1", 0, 0, spanX = -1, spanY = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridItem with zero spanY throws exception`() {
        GridItem("item1", 0, 0, spanX = 1, spanY = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `GridItem with negative spanY throws exception`() {
        GridItem("item1", 0, 0, spanX = 1, spanY = -1)
    }

    @Test
    fun `endX and endY calculate correctly`() {
        val item = GridItem("item1", x = 1, y = 2, spanX = 3, spanY = 4)

        assertEquals(4, item.endX) // 1 + 3
        assertEquals(6, item.endY) // 2 + 4
    }

    @Test
    fun `area calculates correctly`() {
        assertEquals(6, GridItem("item1", 0, 0, 2, 3).area)
        assertEquals(1, GridItem("item1", 0, 0, 1, 1).area)
        assertEquals(12, GridItem("item1", 0, 0, 3, 4).area)
    }

    @Test
    fun `isValidIn returns true for valid items`() {
        val gridSize = GridSize(rows = 5, columns = 4)

        assertTrue(GridItem("item1", 0, 0, 1, 1).isValidIn(gridSize))
        assertTrue(GridItem("item1", 0, 0, 4, 5).isValidIn(gridSize))
        assertTrue(GridItem("item1", 2, 3, 2, 2).isValidIn(gridSize))
    }

    @Test
    fun `isValidIn returns false for invalid items`() {
        val gridSize = GridSize(rows = 5, columns = 4)

        assertFalse(GridItem("item1", -1, 0, 1, 1).isValidIn(gridSize))
        assertFalse(GridItem("item1", 0, -1, 1, 1).isValidIn(gridSize))
        assertFalse(GridItem("item1", 0, 0, 5, 1).isValidIn(gridSize))
        assertFalse(GridItem("item1", 0, 0, 1, 6).isValidIn(gridSize))
        assertFalse(GridItem("item1", 3, 4, 2, 2).isValidIn(gridSize))
    }

    @Test
    fun `occupiesCell returns true for cells within item area`() {
        val item = GridItem("item1", x = 1, y = 2, spanX = 2, spanY = 3)

        assertTrue(item.occupiesCell(1, 2))
        assertTrue(item.occupiesCell(2, 2))
        assertTrue(item.occupiesCell(1, 3))
        assertTrue(item.occupiesCell(2, 4))
    }

    @Test
    fun `occupiesCell returns false for cells outside item area`() {
        val item = GridItem("item1", x = 1, y = 2, spanX = 2, spanY = 3)

        assertFalse(item.occupiesCell(0, 2))
        assertFalse(item.occupiesCell(3, 2))
        assertFalse(item.occupiesCell(1, 1))
        assertFalse(item.occupiesCell(1, 5))
    }

    @Test
    fun `overlapsWith detects overlapping items`() {
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)

        assertTrue(item1.overlapsWith(item2))
        assertTrue(item2.overlapsWith(item1))
    }

    @Test
    fun `overlapsWith returns false for non-overlapping items`() {
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 2, y = 0, spanX = 2, spanY = 2)
        val item3 = GridItem("item3", x = 0, y = 2, spanX = 2, spanY = 2)

        assertFalse(item1.overlapsWith(item2))
        assertFalse(item2.overlapsWith(item1))
        assertFalse(item1.overlapsWith(item3))
        assertFalse(item3.overlapsWith(item1))
    }

    @Test
    fun `moveTo creates new item with updated position`() {
        val original = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val moved = original.moveTo(3, 4)

        assertEquals("item1", moved.id)
        assertEquals(3, moved.x)
        assertEquals(4, moved.y)
        assertEquals(2, moved.spanX)
        assertEquals(2, moved.spanY)
        
        // Original should be unchanged
        assertEquals(0, original.x)
        assertEquals(0, original.y)
    }

    @Test
    fun `resize creates new item with updated span`() {
        val original = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val resized = original.resize(3, 4)

        assertEquals("item1", resized.id)
        assertEquals(0, resized.x)
        assertEquals(0, resized.y)
        assertEquals(3, resized.spanX)
        assertEquals(4, resized.spanY)
        
        // Original should be unchanged
        assertEquals(2, original.spanX)
        assertEquals(2, original.spanY)
    }

    @Test
    fun `single factory method creates 1x1 item`() {
        val item = GridItem.single("item1", x = 2, y = 3)

        assertEquals("item1", item.id)
        assertEquals(2, item.x)
        assertEquals(3, item.y)
        assertEquals(1, item.spanX)
        assertEquals(1, item.spanY)
    }

    @Test
    fun `edge case - items touching but not overlapping`() {
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 2, y = 0, spanX = 2, spanY = 2)

        assertFalse(item1.overlapsWith(item2))
        assertEquals(2, item1.endX)
        assertEquals(2, item2.x)
    }
}
