package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.util.ValidationUtils
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Property-based tests for GridEngine.
 *
 * Verifies that:
 * - On Success: applyTo result has no overlap and all items within bounds
 * - On Failure: input items are not mutated (engine returns without modifying input)
 */
class GridEnginePropertyTest {

    /**
     * Builds a valid non-overlapping grid by placing 1x1 items sequentially (row-major).
     */
    private fun buildValidItems(gridSize: GridSize, count: Int): List<GridItem> {
        val items = mutableListOf<GridItem>()
        var id = 0
        for (y in 0 until gridSize.rows) {
            for (x in 0 until gridSize.columns) {
                if (items.size >= count) return items
                items.add(GridItem("id$id", x, y, 1, 1))
                id++
            }
        }
        return items
    }

    @Test
    fun successfulAddResultHasNoOverlapAndAllWithinBounds() {
        runBlocking {
            checkAll(Arb.int(1..8), Arb.int(1..8), Arb.int(0..15)) { rows, cols, extraId ->
            val gridSize = GridSize(rows, cols)
            val maxItems = (rows * cols) / 2
            val items = buildValidItems(gridSize, maxOf(1, maxItems))
            val newItem = GridItem("new-$extraId", 0, 0, 1, 1)
            val request = EngineRequest.add(newItem, items, gridSize)
            val result = GridEngine.process(request)
            when (result) {
                is EngineResult.Success -> {
                    val applied = result.applyTo(items)
                    assertFalse(
                        ValidationUtils.hasAnyOverlap(applied),
                        "Add success must produce no overlap: $applied"
                    )
                    assertTrue(
                        ValidationUtils.allWithinBounds(applied, gridSize),
                        "Add success must be within bounds: $applied"
                    )
                }
                is EngineResult.Failure -> { /* GridFull - expected */ }
            }
        }
        }
    }

    @Test
    fun successfulMoveResultHasNoOverlapAndAllWithinBounds() {
        runBlocking {
            checkAll(Arb.int(2..8), Arb.int(2..8), Arb.int(0..7), Arb.int(0..7)) { rows, cols, targetX, targetY ->
            val gridSize = GridSize(rows, cols)
            val items = buildValidItems(gridSize, minOf(rows * cols - 1, 8))
            if (items.size < 2) return@checkAll
            val target = items.first()
            val tx = targetX % cols
            val ty = targetY % rows
            val request = EngineRequest.move(target.id, tx, ty, items, gridSize)
            val result = GridEngine.process(request)
            when (result) {
                is EngineResult.Success -> {
                    val applied = result.applyTo(items)
                    assertFalse(
                        ValidationUtils.hasAnyOverlap(applied),
                        "Move success must produce no overlap: $applied"
                    )
                    assertTrue(
                        ValidationUtils.allWithinBounds(applied, gridSize),
                        "Move success must be within bounds: $applied"
                    )
                }
                is EngineResult.Failure -> { /* NoFeasibleLayout or OutOfBounds - expected */ }
            }
        }
        }
    }

    @Test
    fun successfulResizeResultHasNoOverlapAndAllWithinBounds() {
        runBlocking {
            checkAll(Arb.int(2..8), Arb.int(2..8), Arb.int(1..4), Arb.int(1..4)) { rows, cols, spanX, spanY ->
            val gridSize = GridSize(rows, cols)
            val items = buildValidItems(gridSize, minOf(rows * cols - 1, 8))
            if (items.size < 2) return@checkAll
            val target = items.first()
            val newSpanX = maxOf(1, spanX % cols + 1)
            val newSpanY = maxOf(1, spanY % rows + 1)
            val request = EngineRequest.resize(target.id, newSpanX, newSpanY, items, gridSize)
            val result = GridEngine.process(request)
            when (result) {
                is EngineResult.Success -> {
                    val applied = result.applyTo(items)
                    assertFalse(
                        ValidationUtils.hasAnyOverlap(applied),
                        "Resize success must produce no overlap: $applied"
                    )
                    assertTrue(
                        ValidationUtils.allWithinBounds(applied, gridSize),
                        "Resize success must be within bounds: $applied"
                    )
                }
                is EngineResult.Failure -> { /* NoFeasibleLayout or OutOfBounds - expected */ }
            }
        }
        }
    }

    @Test
    fun failureDoesNotMutateInputItems() {
        val gridSize = GridSize(4, 4)
        val fullItems = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val addResult = GridEngine.process(EngineRequest.add(GridItem("e", 0, 0, 1, 1), fullItems, gridSize))
        assertTrue(addResult is EngineResult.Failure)
        val moveResult = GridEngine.process(EngineRequest.move("a", 1, 1, fullItems, gridSize))
        assertTrue(moveResult is EngineResult.Failure)
        val resizeResult = GridEngine.process(EngineRequest.resize("a", 3, 3, fullItems, gridSize))
        assertTrue(resizeResult is EngineResult.Failure)
        assertTrue(fullItems.size == 4)
        assertTrue(fullItems[0].x == 0 && fullItems[0].y == 0)
    }
}
