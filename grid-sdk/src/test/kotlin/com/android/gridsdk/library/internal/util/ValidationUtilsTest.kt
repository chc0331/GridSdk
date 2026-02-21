package com.android.gridsdk.library.internal.util

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isNonNegative returns true for non-negative coordinates`() {
        assertTrue(ValidationUtils.isNonNegative(0, 0))
        assertTrue(ValidationUtils.isNonNegative(5, 3))
        assertTrue(ValidationUtils.isNonNegative(0, 10))
    }

    @Test
    fun `isNonNegative returns false for negative coordinates`() {
        assertFalse(ValidationUtils.isNonNegative(-1, 0))
        assertFalse(ValidationUtils.isNonNegative(0, -1))
        assertFalse(ValidationUtils.isNonNegative(-1, -1))
    }

    @Test
    fun `isValidSpan returns true for valid spans`() {
        assertTrue(ValidationUtils.isValidSpan(1, 1))
        assertTrue(ValidationUtils.isValidSpan(2, 3))
        assertTrue(ValidationUtils.isValidSpan(10, 5))
    }

    @Test
    fun `isValidSpan returns false for invalid spans`() {
        assertFalse(ValidationUtils.isValidSpan(0, 1))
        assertFalse(ValidationUtils.isValidSpan(1, 0))
        assertFalse(ValidationUtils.isValidSpan(-1, 1))
        assertFalse(ValidationUtils.isValidSpan(1, -1))
    }

    @Test
    fun `isWithinBounds returns true for items within grid`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertTrue(ValidationUtils.isWithinBounds(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            gridSize
        ))
        assertTrue(ValidationUtils.isWithinBounds(
            GridItem("item2", x = 0, y = 0, spanX = 4, spanY = 5),
            gridSize
        ))
        assertTrue(ValidationUtils.isWithinBounds(
            GridItem("item3", x = 2, y = 3, spanX = 2, spanY = 2),
            gridSize
        ))
    }

    @Test
    fun `isWithinBounds returns false for items outside grid`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        
        assertFalse(ValidationUtils.isWithinBounds(
            GridItem("item1", x = -1, y = 0, spanX = 1, spanY = 1),
            gridSize
        ))
        assertFalse(ValidationUtils.isWithinBounds(
            GridItem("item2", x = 0, y = 0, spanX = 5, spanY = 1),
            gridSize
        ))
        assertFalse(ValidationUtils.isWithinBounds(
            GridItem("item3", x = 3, y = 4, spanX = 2, spanY = 2),
            gridSize
        ))
    }

    @Test
    fun `hasOverlap detects overlapping items`() {
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)
        
        assertTrue(ValidationUtils.hasOverlap(item1, item2))
    }

    @Test
    fun `hasOverlap returns false for non-overlapping items`() {
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 2, y = 0, spanX = 2, spanY = 2)
        
        assertFalse(ValidationUtils.hasOverlap(item1, item2))
    }

    @Test
    fun `findOverlappingItems returns overlapping items`() {
        val item = GridItem("target", x = 1, y = 1, spanX = 2, spanY = 2)
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("item2", x = 3, y = 0, spanX = 1, spanY = 1),
            GridItem("item3", x = 2, y = 2, spanX = 2, spanY = 2)
        )
        
        val overlapping = ValidationUtils.findOverlappingItems(item, items)
        
        assertEquals(2, overlapping.size)
        assertTrue(overlapping.any { it.id == "item1" })
        assertTrue(overlapping.any { it.id == "item3" })
    }

    @Test
    fun `findOverlappingItems excludes specified item`() {
        val item = GridItem("target", x = 1, y = 1, spanX = 2, spanY = 2)
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("exclude", x = 1, y = 1, spanX = 2, spanY = 2)
        )
        
        val overlapping = ValidationUtils.findOverlappingItems(item, items, excludeItemId = "exclude")
        
        assertEquals(1, overlapping.size)
        assertEquals("item1", overlapping[0].id)
    }

    @Test
    fun `hasAnyOverlap detects overlap in item list`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)
        )
        
        assertTrue(ValidationUtils.hasAnyOverlap(items))
    }

    @Test
    fun `hasAnyOverlap returns false for non-overlapping items`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1),
            GridItem("item3", x = 0, y = 1, spanX = 1, spanY = 1)
        )
        
        assertFalse(ValidationUtils.hasAnyOverlap(items))
    }

    @Test
    fun `allWithinBounds returns true when all items are within grid`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2),
            GridItem("item3", x = 3, y = 4, spanX = 1, spanY = 1)
        )
        
        assertTrue(ValidationUtils.allWithinBounds(items, gridSize))
    }

    @Test
    fun `allWithinBounds returns false when any item is outside grid`() {
        val gridSize = GridSize(rows = 5, columns = 4)
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 3, y = 4, spanX = 2, spanY = 2)
        )
        
        assertFalse(ValidationUtils.allWithinBounds(items, gridSize))
    }

    @Test
    fun `findItemAtCell returns item occupying the cell`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("item2", x = 2, y = 0, spanX = 1, spanY = 1)
        )
        
        val item = ValidationUtils.findItemAtCell(1, 1, items)
        
        assertNotNull(item)
        assertEquals("item1", item?.id)
    }

    @Test
    fun `findItemAtCell returns null for empty cell`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        )
        
        val item = ValidationUtils.findItemAtCell(2, 2, items)
        
        assertNull(item)
    }

    @Test
    fun `hasUniqueIds returns true for unique IDs`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1),
            GridItem("item3", x = 2, y = 0, spanX = 1, spanY = 1)
        )
        
        assertTrue(ValidationUtils.hasUniqueIds(items))
    }

    @Test
    fun `hasUniqueIds returns false for duplicate IDs`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1),
            GridItem("item1", x = 2, y = 0, spanX = 1, spanY = 1)
        )
        
        assertFalse(ValidationUtils.hasUniqueIds(items))
    }

    @Test
    fun `isAreaEmpty returns true for empty area`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        )
        
        assertTrue(ValidationUtils.isAreaEmpty(2, 2, 2, 2, items))
    }

    @Test
    fun `isAreaEmpty returns false for occupied area`() {
        val items = listOf(
            GridItem("item1", x = 1, y = 1, spanX = 2, spanY = 2)
        )
        
        assertFalse(ValidationUtils.isAreaEmpty(0, 0, 2, 2, items))
    }

    @Test
    fun `isAreaEmpty excludes specified item`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("exclude", x = 1, y = 1, spanX = 1, spanY = 1)
        )
        
        assertFalse(ValidationUtils.isAreaEmpty(1, 1, 2, 2, items, excludeItemId = "exclude"))
    }
}
