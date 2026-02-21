package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class GridEngineAddTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `add to empty grid places item at 0,0`() {
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("a", success.targetItem.id)
        assertEquals(0, success.targetItem.x)
        assertEquals(0, success.targetItem.y)
        assertEquals(1, success.targetItem.spanX)
        assertEquals(1, success.targetItem.spanY)
        assertTrue(success.relocatedItems.isEmpty())
    }

    @Test
    fun `add to partially occupied grid places at first empty top-left`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2)
        )
        val newItem = GridItem("b", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("b", success.targetItem.id)
        assertEquals(2, success.targetItem.x)
        assertEquals(0, success.targetItem.y)
    }

    @Test
    fun `add with span considers first fitting slot`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2)
        )
        val newItem = GridItem("b", 0, 0, 2, 2)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("b", success.targetItem.id)
        // 상단-좌측 순: a가 (0,0)-(1,1) 점유 → 첫 2x2 빈 슬롯은 (2,0)
        assertEquals(2, success.targetItem.x)
        assertEquals(0, success.targetItem.y)
        assertEquals(2, success.targetItem.spanX)
        assertEquals(2, success.targetItem.spanY)
    }

    @Test
    fun `add to full grid returns GridFull failure`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val newItem = GridItem("e", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.GridFull)
    }

    @Test
    fun `add with duplicate id returns DuplicateItemId failure`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val newItem = GridItem("a", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.DuplicateItemId)
        assertEquals("a", (failure.error as GridError.DuplicateItemId).itemId)
    }

    @Test
    fun `add success applyTo returns updated item list`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val newItem = GridItem("b", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        val applied = success.applyTo(items)
        assertEquals(2, applied.size)
        assertTrue(applied.any { it.id == "a" })
        assertTrue(applied.any { it.id == "b" })
        val b = applied.find { it.id == "b" }!!
        assertEquals(1, b.x)
        assertEquals(0, b.y)
    }

    @Test
    fun `add failure does not modify original items list`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val originalCopy = items.toList()
        val newItem = GridItem("e", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        assertEquals(originalCopy.size, items.size)
        items.forEachIndexed { index, item ->
            assertEquals(originalCopy[index].id, item.id)
            assertEquals(originalCopy[index].x, item.x)
            assertEquals(originalCopy[index].y, item.y)
            assertEquals(originalCopy[index].spanX, item.spanX)
            assertEquals(originalCopy[index].spanY, item.spanY)
        }
    }

    @Test
    fun `add failure with duplicate id does not modify original items`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val originalCopy = items.toList()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val request = EngineRequest.add(newItem, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        assertEquals(1, items.size)
        assertEquals(originalCopy[0].id, items[0].id)
        assertEquals(originalCopy[0].x, items[0].x)
        assertEquals(originalCopy[0].y, items[0].y)
    }
}
