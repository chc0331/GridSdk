package com.android.gridsdk.library.model

import org.junit.Assert.*
import org.junit.Test

class GridErrorTest {

    @Test
    fun `OutOfBounds contains correct information`() {
        val error = GridError.OutOfBounds(
            itemId = "item1",
            position = GridError.Position(x = 5, y = 3, spanX = 2, spanY = 2),
            gridSize = GridSize(rows = 5, columns = 4)
        )

        assertEquals("item1", error.itemId)
        assertEquals(5, error.position.x)
        assertEquals(3, error.position.y)
        assertEquals(2, error.position.spanX)
        assertEquals(2, error.position.spanY)
        assertEquals(5, error.gridSize.rows)
        assertEquals(4, error.gridSize.columns)
    }

    @Test
    fun `OutOfBounds toString is descriptive`() {
        val error = GridError.OutOfBounds(
            itemId = "item1",
            position = GridError.Position(x = 5, y = 3, spanX = 2, spanY = 2),
            gridSize = GridSize(rows = 5, columns = 4)
        )

        val message = error.toString()
        assertTrue(message.contains("item1"))
        assertTrue(message.contains("5"))
        assertTrue(message.contains("3"))
    }

    @Test
    fun `NoFeasibleLayout contains reason and conflicts`() {
        val error = GridError.NoFeasibleLayout(
            itemId = "item1",
            reason = "No space available",
            conflictingItems = listOf("item2", "item3")
        )

        assertEquals("item1", error.itemId)
        assertEquals("No space available", error.reason)
        assertEquals(2, error.conflictingItems.size)
        assertTrue(error.conflictingItems.contains("item2"))
        assertTrue(error.conflictingItems.contains("item3"))
    }

    @Test
    fun `NoFeasibleLayout can have empty conflicts list`() {
        val error = GridError.NoFeasibleLayout(
            itemId = "item1",
            reason = "Grid is full"
        )

        assertTrue(error.conflictingItems.isEmpty())
    }

    @Test
    fun `NoFeasibleLayout toString shows conflicts when present`() {
        val error = GridError.NoFeasibleLayout(
            itemId = "item1",
            reason = "Cannot fit",
            conflictingItems = listOf("item2")
        )

        val message = error.toString()
        assertTrue(message.contains("item1"))
        assertTrue(message.contains("Cannot fit"))
        assertTrue(message.contains("item2"))
    }

    @Test
    fun `ItemOverlap contains overlapping items`() {
        val error = GridError.ItemOverlap(
            itemId = "item1",
            overlappingItems = listOf("item2", "item3", "item4")
        )

        assertEquals("item1", error.itemId)
        assertEquals(3, error.overlappingItems.size)
    }

    @Test
    fun `ItemOverlap toString lists all overlapping items`() {
        val error = GridError.ItemOverlap(
            itemId = "item1",
            overlappingItems = listOf("item2", "item3")
        )

        val message = error.toString()
        assertTrue(message.contains("item1"))
        assertTrue(message.contains("item2"))
        assertTrue(message.contains("item3"))
    }

    @Test
    fun `ItemNotFound contains item ID`() {
        val error = GridError.ItemNotFound("missing_item")

        assertEquals("missing_item", error.itemId)
    }

    @Test
    fun `ItemNotFound toString is descriptive`() {
        val error = GridError.ItemNotFound("missing_item")

        val message = error.toString()
        assertTrue(message.contains("missing_item"))
        assertTrue(message.contains("not exist") || message.contains("NotFound"))
    }

    @Test
    fun `InvalidItem contains reason`() {
        val error = GridError.InvalidItem(
            itemId = "item1",
            reason = "Negative coordinates"
        )

        assertEquals("item1", error.itemId)
        assertEquals("Negative coordinates", error.reason)
    }

    @Test
    fun `InvalidItem toString shows reason`() {
        val error = GridError.InvalidItem(
            itemId = "item1",
            reason = "Invalid span"
        )

        val message = error.toString()
        assertTrue(message.contains("item1"))
        assertTrue(message.contains("Invalid span"))
    }

    @Test
    fun `GridFull contains grid size`() {
        val error = GridError.GridFull(GridSize(rows = 5, columns = 4))

        assertEquals(5, error.gridSize.rows)
        assertEquals(4, error.gridSize.columns)
    }

    @Test
    fun `GridFull toString is descriptive`() {
        val error = GridError.GridFull(GridSize(rows = 5, columns = 4))

        val message = error.toString()
        assertTrue(message.contains("5"))
        assertTrue(message.contains("4"))
        assertTrue(message.contains("full") || message.contains("Full"))
    }

    @Test
    fun `DuplicateItemId contains item ID`() {
        val error = GridError.DuplicateItemId("duplicate_id")

        assertEquals("duplicate_id", error.itemId)
    }

    @Test
    fun `DuplicateItemId toString is descriptive`() {
        val error = GridError.DuplicateItemId("duplicate_id")

        val message = error.toString()
        assertTrue(message.contains("duplicate_id"))
        assertTrue(message.contains("duplicate") || message.contains("Duplicate"))
    }

    @Test
    fun `Position data class works correctly`() {
        val position = GridError.Position(x = 2, y = 3, spanX = 4, spanY = 5)

        assertEquals(2, position.x)
        assertEquals(3, position.y)
        assertEquals(4, position.spanX)
        assertEquals(5, position.spanY)
    }

    @Test
    fun `Position toString is descriptive`() {
        val position = GridError.Position(x = 2, y = 3, spanX = 4, spanY = 5)

        val message = position.toString()
        assertTrue(message.contains("2"))
        assertTrue(message.contains("3"))
        assertTrue(message.contains("4"))
        assertTrue(message.contains("5"))
    }

    @Test
    fun `GridError is sealed class - exhaustive when check`() {
        val errors: List<GridError> = listOf(
            GridError.OutOfBounds("id", GridError.Position(0, 0, 1, 1), GridSize(5, 4)),
            GridError.NoFeasibleLayout("id", "reason"),
            GridError.ItemOverlap("id", listOf("other")),
            GridError.ItemNotFound("id"),
            GridError.InvalidItem("id", "reason"),
            GridError.GridFull(GridSize(5, 4)),
            GridError.DuplicateItemId("id")
        )

        // This should compile without 'else' branch
        errors.forEach { error ->
            when (error) {
                is GridError.OutOfBounds -> assertEquals("id", error.itemId)
                is GridError.NoFeasibleLayout -> assertEquals("id", error.itemId)
                is GridError.ItemOverlap -> assertEquals("id", error.itemId)
                is GridError.ItemNotFound -> assertEquals("id", error.itemId)
                is GridError.InvalidItem -> assertEquals("id", error.itemId)
                is GridError.GridFull -> assertNotNull(error.gridSize)
                is GridError.DuplicateItemId -> assertEquals("id", error.itemId)
            }
        }
    }

    @Test
    fun `GridError subclasses are data classes with proper equality`() {
        val error1 = GridError.ItemNotFound("item1")
        val error2 = GridError.ItemNotFound("item1")
        val error3 = GridError.ItemNotFound("item2")

        assertEquals(error1, error2)
        assertNotEquals(error1, error3)
    }
}
