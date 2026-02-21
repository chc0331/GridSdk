package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class GridEngineResizeTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `resize expand with no conflict succeeds`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 2, 2, 2, 2)
        )
        val request = EngineRequest.resize("a", 2, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals("a", success.targetItem.id)
        assertEquals(0, success.targetItem.x)
        assertEquals(0, success.targetItem.y)
        assertEquals(2, success.targetItem.spanX)
        assertEquals(2, success.targetItem.spanY)
        assertTrue(success.relocatedItems.isEmpty())
    }

    @Test
    fun `resize shrink with no conflict succeeds`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 2, 2, 2)
        )
        val request = EngineRequest.resize("a", 1, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(1, success.targetItem.spanX)
        assertEquals(1, success.targetItem.spanY)
        assertTrue(success.relocatedItems.isEmpty())
    }

    @Test
    fun `resize expand with conflict relocates conflicting item`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        val request = EngineRequest.resize("a", 2, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(2, success.targetItem.spanX)
        assertEquals(1, success.targetItem.spanY)
        assertEquals(1, success.relocatedItems.size)
        val relocatedB = success.relocatedItems.find { it.id == "b" }!!
        assertFalse(relocatedB.x == 1 && relocatedB.y == 0)
    }

    @Test
    fun `resize to out of bounds returns OutOfBounds`() {
        val items = listOf(GridItem("a", 0, 0, 2, 2))
        val request = EngineRequest.resize("a", 5, 5, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.OutOfBounds)
        val outOfBounds = failure.error as GridError.OutOfBounds
        assertEquals("a", outOfBounds.itemId)
        assertEquals(5, outOfBounds.position.spanX)
        assertEquals(5, outOfBounds.position.spanY)
    }

    @Test
    fun `resize with invalid span returns InvalidItem`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val request = EngineRequest.resize("a", 0, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.InvalidItem)
        assertEquals("a", (failure.error as GridError.InvalidItem).itemId)
    }

    @Test
    fun `resize with negative span returns InvalidItem`() {
        val items = listOf(GridItem("a", 0, 0, 2, 2))
        val request = EngineRequest.resize("a", -1, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.InvalidItem)
    }

    @Test
    fun `resize when no feasible layout returns NoFeasibleLayout`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val request = EngineRequest.resize("a", 3, 3, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.NoFeasibleLayout)
        assertEquals("a", (failure.error as GridError.NoFeasibleLayout).itemId)
    }

    @Test
    fun `resize with non-existent item returns ItemNotFound`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val request = EngineRequest.resize("nonexistent", 2, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        val failure = result as EngineResult.Failure
        assertTrue(failure.error is GridError.ItemNotFound)
        assertEquals("nonexistent", (failure.error as GridError.ItemNotFound).itemId)
    }

    @Test
    fun `resize success applyTo returns updated list`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 2, 2, 2, 2)
        )
        val request = EngineRequest.resize("a", 2, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        val applied = success.applyTo(items)
        assertEquals(2, applied.size)
        val a = applied.find { it.id == "a" }!!
        assertEquals(2, a.spanX)
        assertEquals(2, a.spanY)
    }

    @Test
    fun `resize failure does not modify original items`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val originalCopy = items.map { it.copy() }
        val request = EngineRequest.resize("a", 3, 3, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Failure)
        assertEquals(originalCopy.size, items.size)
        items.forEachIndexed { index, item ->
            assertEquals(originalCopy[index].id, item.id)
            assertEquals(originalCopy[index].spanX, item.spanX)
            assertEquals(originalCopy[index].spanY, item.spanY)
        }
    }

    @Test
    fun `resize with multiple conflicts relocates minimally`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1),
            GridItem("c", 2, 0, 1, 1)
        )
        val request = EngineRequest.resize("a", 3, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(3, success.targetItem.spanX)
        assertEquals(1, success.targetItem.spanY)
        assertEquals(2, success.relocatedItems.size)
    }

    @Test
    fun `resize result minimizes relocated item count - single conflict relocates exactly one`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        val request = EngineRequest.resize("a", 2, 1, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(2, success.targetItem.spanX)
        assertEquals(1, success.targetItem.spanY)
        assertEquals(1, success.relocatedItems.size)
    }

    @Test
    fun `resize result minimizes relocated item count - two conflicts relocates exactly two`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1),
            GridItem("c", 0, 1, 1, 1)
        )
        val request = EngineRequest.resize("a", 2, 2, items, gridSize)

        val result = GridEngine.process(request)

        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertEquals(2, success.targetItem.spanX)
        assertEquals(2, success.targetItem.spanY)
        assertEquals(2, success.relocatedItems.size)
    }
}
