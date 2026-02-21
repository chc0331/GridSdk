package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for state immutability on engine failure.
 *
 * PRD: "실패 시 항상 기존 상태를 보존한다"
 * Verifies that when Add/Move/Resize fails, the input items list is never mutated.
 */
class GridEngineStateInvariantTest {

    private val gridSize = GridSize(4, 4)

    @Test
    fun `Add failure - GridFull - items unchanged`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.add(GridItem("e", 0, 0, 1, 1), items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.GridFull)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Add failure - DuplicateItemId - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.add(GridItem("a", 0, 0, 1, 1), items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.DuplicateItemId)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Move failure - OutOfBounds - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.move("a", 10, 10, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.OutOfBounds)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Move failure - NoFeasibleLayout - items unchanged`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.move("a", 1, 1, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.NoFeasibleLayout)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Move failure - ItemNotFound - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.move("nonexistent", 1, 1, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.ItemNotFound)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Resize failure - OutOfBounds - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 2, 2))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.resize("a", 5, 5, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.OutOfBounds)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Resize failure - InvalidItem - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.resize("a", 0, 1, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.InvalidItem)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Resize failure - NoFeasibleLayout - items unchanged`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.resize("a", 3, 3, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.NoFeasibleLayout)
        assertItemsUnchanged(original, items)
    }

    @Test
    fun `Resize failure - ItemNotFound - items unchanged`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val original = items.map { it.copy() }
        val result = GridEngine.process(EngineRequest.resize("nonexistent", 2, 2, items, gridSize))
        assertTrue(result is EngineResult.Failure)
        assertTrue((result as EngineResult.Failure).error is GridError.ItemNotFound)
        assertItemsUnchanged(original, items)
    }

    private fun assertItemsUnchanged(original: List<GridItem>, actual: List<GridItem>) {
        assertEquals(original.size, actual.size)
        original.forEachIndexed { index, orig ->
            val item = actual[index]
            assertEquals(orig.id, item.id)
            assertEquals(orig.x, item.x)
            assertEquals(orig.y, item.y)
            assertEquals(orig.spanX, item.spanX)
            assertEquals(orig.spanY, item.spanY)
        }
    }
}
