package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Performance smoke tests for GridEngine.
 *
 * Verifies that process() completes within acceptable time as item count increases.
 * PRD: "배치 계산은 결정적(deterministic)이고 빠르게 동작해야 한다"
 */
class GridEnginePerformanceSmokeTest {

    private val gridSize = GridSize(8, 8)

    /**
     * Builds items placed sequentially (row-major) without overlap.
     */
    private fun buildItems(count: Int): List<GridItem> {
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
    fun `process move with 4 items completes within 100ms`() {
        val items = buildItems(4)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.move("id0", 1, 1, items, gridSize))
        }
        assertTrue("Move with 4 items took ${elapsed}ms", elapsed < 100)
    }

    @Test
    fun `process move with 8 items completes within 100ms`() {
        val items = buildItems(8)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.move("id0", 2, 2, items, gridSize))
        }
        assertTrue("Move with 8 items took ${elapsed}ms", elapsed < 100)
    }

    @Test
    fun `process move with 16 items completes within 500ms`() {
        val items = buildItems(16)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.move("id0", 3, 3, items, gridSize))
        }
        assertTrue("Move with 16 items took ${elapsed}ms", elapsed < 500)
    }

    @Test
    fun `process move with 32 items completes within 1000ms`() {
        val items = buildItems(32)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.move("id0", 4, 4, items, gridSize))
        }
        assertTrue("Move with 32 items took ${elapsed}ms", elapsed < 1000)
    }

    @Test
    fun `process add with 8 items completes within 100ms`() {
        val items = buildItems(8)
        val newItem = GridItem("new", 0, 0, 1, 1)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.add(newItem, items, gridSize))
        }
        assertTrue("Add with 8 items took ${elapsed}ms", elapsed < 100)
    }

    @Test
    fun `process resize with 8 items completes within 100ms`() {
        val items = buildItems(8)
        val elapsed = measureTimeMillis {
            GridEngine.process(EngineRequest.resize("id0", 2, 2, items, gridSize))
        }
        assertTrue("Resize with 8 items took ${elapsed}ms", elapsed < 100)
    }
}
