package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class GridEngineMoveTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `move with no conflict succeeds`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 2, 2, 2)
        )
        val request = EngineRequest.move("a", 0, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("a", success.targetItem.id)
        assertEquals(0, success.targetItem.x)
        assertEquals(2, success.targetItem.y)
        assertEquals(2, success.targetItem.spanX)
        assertEquals(2, success.targetItem.spanY)
        assertTrue(success.relocatedItems.isEmpty())
    }

    @Test
    fun `move with single conflict relocates one item`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 1, 1, 2, 2)
        )
        val request = EngineRequest.move("a", 1, 0, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("a", success.targetItem.id)
        assertEquals(1, success.targetItem.x)
        assertEquals(0, success.targetItem.y)
        assertEquals(1, success.relocatedItems.size)
        val relocatedB = success.relocatedItems.find { it.id == "b" }!!
        assertFalse(relocatedB.x == 1 && relocatedB.y == 1)
    }

    @Test
    fun `move with multiple conflicts relocates minimally`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1),
            GridItem("c", 2, 0, 1, 1)
        )
        val request = EngineRequest.move("a", 1, 0, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(1, success.relocatedItems.size)
    }

    @Test
    fun `move to out of bounds returns OutOfBounds`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val request = EngineRequest.move("a", 5, 5, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.OutOfBounds)
        val outOfBounds = failure.error as GridError.OutOfBounds
        assertEquals("a", outOfBounds.itemId)
        assertEquals(5, outOfBounds.position.x)
        assertEquals(5, outOfBounds.position.y)
    }

    @Test
    fun `move when no feasible layout returns NoFeasibleLayout`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val request = EngineRequest.move("a", 1, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.NoFeasibleLayout)
        assertEquals("a", (failure.error as GridError.NoFeasibleLayout).itemId)
    }

    @Test
    fun `move with non-existent item returns ItemNotFound`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val request = EngineRequest.move("nonexistent", 1, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.ItemNotFound)
        assertEquals("nonexistent", (failure.error as GridError.ItemNotFound).itemId)
    }

    @Test
    fun `move success applyTo returns updated list`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 2, 2, 2)
        )
        val request = EngineRequest.move("a", 0, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        val applied = success.applyTo(items)
        assertEquals(2, applied.size)
        val a = applied.find { it.id == "a" }!!
        assertEquals(0, a.x)
        assertEquals(2, a.y)
    }

    @Test
    fun `move failure does not modify original items`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val originalCopy = items.map { it.copy() }
        val request = EngineRequest.move("a", 1, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        assertEquals(originalCopy.size, items.size)
        items.forEachIndexed { index, item ->
            assertEquals(originalCopy[index].id, item.id)
            assertEquals(originalCopy[index].x, item.x)
            assertEquals(originalCopy[index].y, item.y)
        }
    }

    @Test
    fun `move tie-break prefers top-left when multiple candidates have same score`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 1, 1, 1)
        )
        val request = EngineRequest.move("a", 1, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        val relocatedB = success.relocatedItems.find { it.id == "b" }!!
        assertEquals(1, relocatedB.x)
        assertEquals(0, relocatedB.y)
    }
}
