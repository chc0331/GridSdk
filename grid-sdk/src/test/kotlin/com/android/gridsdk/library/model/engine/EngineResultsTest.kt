package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class EngineResultsTest {

    @Test
    fun `Success contains targetItem and relocatedItems`() {
        val target = GridItem("item1", x = 2, y = 1, spanX = 2, spanY = 2)
        val relocated = listOf(
            GridItem("item2", x = 0, y = 2, spanX = 1, spanY = 1)
        )
        val result = EngineResult.Success(target, relocated)

        assertEquals(target, result.targetItem)
        assertEquals(1, result.relocatedItems.size)
        assertEquals("item2", result.relocatedItems[0].id)
        assertEquals(0, result.relocatedItems[0].x)
        assertEquals(2, result.relocatedItems[0].y)
    }

    @Test
    fun `Success can have empty relocatedItems`() {
        val target = GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        val result = EngineResult.Success(target, emptyList())

        assertTrue(result.relocatedItems.isEmpty())
    }

    @Test
    fun `Success applyTo merges with original list`() {
        val original = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
            GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1),
            GridItem("item3", x = 0, y = 1, spanX = 1, spanY = 1)
        )
        val target = GridItem("item1", x = 2, y = 2, spanX = 1, spanY = 1)
        val relocated = listOf(
            GridItem("item2", x = 0, y = 0, spanX = 1, spanY = 1)
        )
        val result = EngineResult.Success(target, relocated)
        val applied = result.applyTo(original)

        assertEquals(3, applied.size)
        val byId = applied.associateBy { it.id }
        assertEquals(2, byId["item1"]!!.x)
        assertEquals(2, byId["item1"]!!.y)
        assertEquals(0, byId["item2"]!!.x)
        assertEquals(0, byId["item2"]!!.y)
        assertEquals(0, byId["item3"]!!.x)
        assertEquals(1, byId["item3"]!!.y)
    }

    @Test
    fun `Success applyTo adds new item when not in original`() {
        val original = listOf(
            GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        )
        val newItem = GridItem("item2", x = 1, y = 0, spanX = 1, spanY = 1)
        val result = EngineResult.Success(newItem, emptyList())
        val applied = result.applyTo(original)

        assertEquals(2, applied.size)
        assertTrue(applied.any { it.id == "item2" })
    }

    @Test
    fun `Failure contains error`() {
        val error = GridError.ItemNotFound("missing")
        val result = EngineResult.Failure(error)

        assertEquals(error, result.error)
        assertTrue(result.error is GridError.ItemNotFound)
    }

    @Test
    fun `companion success creates Success`() {
        val target = GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        val result = EngineResult.success(target)

        assertTrue(result is EngineResult.Success)
        assertEquals(target, result.targetItem)
        assertTrue(result.relocatedItems.isEmpty())
    }

    @Test
    fun `companion success with relocatedItems`() {
        val target = GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1)
        val relocated = listOf(GridItem("item2", x = 1, y = 1, spanX = 1, spanY = 1))
        val result = EngineResult.success(target, relocated)

        assertEquals(1, result.relocatedItems.size)
    }

    @Test
    fun `companion failure creates Failure`() {
        val error = GridError.NoFeasibleLayout("item1", "No space")
        val result = EngineResult.failure(error)

        assertTrue(result is EngineResult.Failure)
        assertEquals(error, result.error)
    }

    @Test
    fun `EngineResult exhaustive when`() {
        val results: List<EngineResult> = listOf(
            EngineResult.Success(
                GridItem("item1", 0, 0, 1, 1),
                emptyList()
            ),
            EngineResult.Failure(GridError.ItemNotFound("x"))
        )
        results.forEach { result ->
            when (result) {
                is EngineResult.Success -> assertNotNull(result.targetItem)
                is EngineResult.Failure -> assertNotNull(result.error)
            }
        }
    }
}
