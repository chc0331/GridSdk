package com.android.gridsdk.library.internal.state

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class OccupancyGridTest {

    @Test
    fun `empty grid has all cells empty`() {
        val occupancy = OccupancyGrid(GridSize(rows = 3, columns = 3))

        for (row in 0 until 3) {
            for (col in 0 until 3) {
                assertTrue(occupancy.isEmpty(col, row))
                assertNull(occupancy.getOccupant(col, row))
            }
        }
    }

    @Test
    fun `place item marks cells as occupied`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item)

        assertFalse(occupancy.isEmpty(1, 1))
        assertFalse(occupancy.isEmpty(2, 1))
        assertFalse(occupancy.isEmpty(1, 2))
        assertFalse(occupancy.isEmpty(2, 2))

        assertEquals("item1", occupancy.getOccupant(1, 1))
        assertEquals("item1", occupancy.getOccupant(2, 2))
    }

    @Test
    fun `place item does not affect other cells`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 1, y = 1, spanX = 1, spanY = 1)

        occupancy.place(item)

        assertTrue(occupancy.isEmpty(0, 0))
        assertTrue(occupancy.isEmpty(2, 1))
        assertTrue(occupancy.isEmpty(1, 2))
    }

    @Test(expected = IllegalStateException::class)
    fun `place item on occupied cell throws exception`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item1)
        occupancy.place(item2) // Should throw
    }

    @Test
    fun `remove item clears occupied cells`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item)
        occupancy.remove(item)

        assertTrue(occupancy.isEmpty(1, 1))
        assertTrue(occupancy.isEmpty(2, 1))
        assertTrue(occupancy.isEmpty(1, 2))
        assertTrue(occupancy.isEmpty(2, 2))
    }

    @Test
    fun `removeById clears all cells with that ID`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 0, y = 0, spanX = 3, spanY = 2)

        occupancy.place(item)
        occupancy.removeById("item1")

        for (row in 0 until 2) {
            for (col in 0 until 3) {
                assertTrue(occupancy.isEmpty(col, row))
            }
        }
    }

    @Test
    fun `canPlace returns true for valid empty area`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)

        assertTrue(occupancy.canPlace(item))
    }

    @Test
    fun `canPlace returns false for occupied area`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item1)

        assertFalse(occupancy.canPlace(item2))
    }

    @Test
    fun `canPlace returns false for out of bounds`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 3, y = 4, spanX = 2, spanY = 2)

        assertFalse(occupancy.canPlace(item))
    }

    @Test
    fun `canPlace excludes specified item`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item)

        // Same item at same location should be allowed (for move validation)
        assertTrue(occupancy.canPlace(item, excludeItemId = "item1"))
    }

    @Test
    fun `getConflictingItems returns empty set for no conflicts`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)

        val conflicts = occupancy.getConflictingItems(item)

        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `getConflictingItems returns conflicting item IDs`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)
        val item3 = GridItem("item3", x = 2, y = 2, spanX = 1, spanY = 1)

        occupancy.place(item1)
        occupancy.place(item3)

        val conflicts = occupancy.getConflictingItems(item2)

        assertEquals(2, conflicts.size)
        assertTrue(conflicts.contains("item1"))
        assertTrue(conflicts.contains("item3"))
    }

    @Test
    fun `getConflictingItems excludes specified item`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)

        occupancy.place(item1)

        val conflicts = occupancy.getConflictingItems(item2, excludeItemId = "item1")

        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun `isAreaEmpty returns true for empty area`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))

        assertTrue(occupancy.isAreaEmpty(0, 0, 2, 2))
    }

    @Test
    fun `isAreaEmpty returns false for occupied area`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 1, y = 1, spanX = 1, spanY = 1)

        occupancy.place(item)

        assertFalse(occupancy.isAreaEmpty(0, 0, 3, 3))
    }

    @Test
    fun `isAreaEmpty returns false for out of bounds`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))

        assertFalse(occupancy.isAreaEmpty(3, 4, 2, 2))
    }

    @Test
    fun `clear removes all items`() {
        val occupancy = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item1 = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)
        val item2 = GridItem("item2", x = 2, y = 2, spanX = 1, spanY = 1)

        occupancy.place(item1)
        occupancy.place(item2)
        occupancy.clear()

        assertTrue(occupancy.isEmpty(0, 0))
        assertTrue(occupancy.isEmpty(1, 1))
        assertTrue(occupancy.isEmpty(2, 2))
    }

    @Test
    fun `copy creates independent instance`() {
        val original = OccupancyGrid(GridSize(rows = 5, columns = 4))
        val item = GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2)

        original.place(item)
        val copy = original.copy()

        // Copy should have the same state
        assertEquals("item1", copy.getOccupant(0, 0))

        // Modifying original should not affect copy
        original.remove(item)
        assertEquals("item1", copy.getOccupant(0, 0))
        assertTrue(original.isEmpty(0, 0))
    }

    @Test
    fun `fromItems creates grid with all items placed`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1),
            GridItem("item3", x = 0, y = 1, spanX = 2, spanY = 1)
        )

        val occupancy = OccupancyGrid.fromItems(GridSize(rows = 5, columns = 4), items)

        assertEquals("item1", occupancy.getOccupant(0, 0))
        assertEquals("item2", occupancy.getOccupant(1, 0))
        assertEquals("item3", occupancy.getOccupant(0, 1))
        assertEquals("item3", occupancy.getOccupant(1, 1))
    }

    @Test(expected = IllegalStateException::class)
    fun `fromItems throws on overlapping items`() {
        val items = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 2, spanY = 2),
            GridItem("item2", x = 1, y = 1, spanX = 2, spanY = 2)
        )

        OccupancyGrid.fromItems(GridSize(rows = 5, columns = 4), items)
    }

    @Test
    fun `isEmpty returns false for out of bounds coordinates`() {
        val occupancy = OccupancyGrid(GridSize(rows = 3, columns = 3))

        assertFalse(occupancy.isEmpty(-1, 0))
        assertFalse(occupancy.isEmpty(0, -1))
        assertFalse(occupancy.isEmpty(3, 0))
        assertFalse(occupancy.isEmpty(0, 3))
    }

    @Test
    fun `getOccupant returns null for out of bounds coordinates`() {
        val occupancy = OccupancyGrid(GridSize(rows = 3, columns = 3))

        assertNull(occupancy.getOccupant(-1, 0))
        assertNull(occupancy.getOccupant(0, -1))
        assertNull(occupancy.getOccupant(3, 0))
        assertNull(occupancy.getOccupant(0, 3))
    }
}
