package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for various GridSize (N x M) combinations.
 *
 * Ensures Add, Move, Resize work correctly when grid dimensions change.
 */
class GridEngineGridSizeRegressionTest {

    @Test
    fun `add to 1x1 empty grid succeeds`() {
        val gridSize = GridSize(1, 1)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
        val success = result as EngineResult.Success
        assertTrue(success.targetItem.x == 0 && success.targetItem.y == 0)
    }

    @Test
    fun `add to 2x2 empty grid succeeds`() {
        val gridSize = GridSize(2, 2)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `add to 4x4 empty grid succeeds`() {
        val gridSize = GridSize(4, 4)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `add to 8x8 empty grid succeeds`() {
        val gridSize = GridSize(8, 8)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `add to 3x5 empty grid succeeds`() {
        val gridSize = GridSize(3, 5)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `add to 2x10 empty grid succeeds`() {
        val gridSize = GridSize(2, 10)
        val items = emptyList<GridItem>()
        val newItem = GridItem("a", 0, 0, 1, 1)
        val result = GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `move in 1x1 grid - single item stays`() {
        val gridSize = GridSize(1, 1)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val result = GridEngine.process(EngineRequest.move("a", 0, 0, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `move in 2x2 grid succeeds`() {
        val gridSize = GridSize(2, 2)
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 1, 1, 1)
        )
        val result = GridEngine.process(EngineRequest.move("a", 1, 0, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `move in 4x4 grid succeeds`() {
        val gridSize = GridSize(4, 4)
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 2, 2, 1, 1)
        )
        val result = GridEngine.process(EngineRequest.move("a", 3, 3, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `move in 8x8 grid succeeds`() {
        val gridSize = GridSize(8, 8)
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 4, 4, 1, 1)
        )
        val result = GridEngine.process(EngineRequest.move("a", 7, 7, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `move in 3x5 grid succeeds`() {
        val gridSize = GridSize(3, 5)
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 2, 2, 1, 1)
        )
        val result = GridEngine.process(EngineRequest.move("a", 4, 2, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `resize in 2x2 grid succeeds`() {
        val gridSize = GridSize(2, 2)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val result = GridEngine.process(EngineRequest.resize("a", 2, 2, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `resize in 4x4 grid succeeds`() {
        val gridSize = GridSize(4, 4)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val result = GridEngine.process(EngineRequest.resize("a", 2, 2, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `resize in 8x8 grid succeeds`() {
        val gridSize = GridSize(8, 8)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val result = GridEngine.process(EngineRequest.resize("a", 3, 3, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }

    @Test
    fun `resize in 3x5 grid succeeds`() {
        val gridSize = GridSize(3, 5)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val result = GridEngine.process(EngineRequest.resize("a", 2, 2, items, gridSize))
        assertTrue(result is EngineResult.Success)
    }
}
