package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for rollback rules.
 *
 * PRD: "복귀 판단은 현재 프레임의 점유 상태 기준으로 수행한다"
 * Verifies:
 * - Rollback result has no overlap and all items within bounds
 * - Items not in relocatedWithOriginals are unchanged
 */
class GridEngineRollbackRegressionTest {

    private val gridSize = GridSize(4, 4)

    @Test
    fun `rollback result has no overlap`() {
        val originalB = GridItem("b", 1, 1, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 0, 3, 2, 1),
            GridItem("b", 0, 0, 2, 2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        assertFalse(
            "Rollback result must have no overlap: $result",
            ValidationUtils.hasAnyOverlap(result)
        )
    }

    @Test
    fun `rollback result all within bounds`() {
        val originalB = GridItem("b", 1, 1, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 0, 3, 2, 1),
            GridItem("b", 0, 0, 2, 2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        assertTrue(
            "Rollback result must be within bounds: $result",
            ValidationUtils.allWithinBounds(result, gridSize)
        )
    }

    @Test
    fun `items not in relocatedWithOriginals are unchanged`() {
        val originalB = GridItem("b", 1, 1, 2, 2)
        val itemA = GridItem("a", 0, 3, 2, 1)
        val currentLayout = listOf(
            itemA,
            GridItem("b", 0, 0, 2, 2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        val resultA = result.find { it.id == "a" }!!
        assertEquals(itemA.x, resultA.x)
        assertEquals(itemA.y, resultA.y)
        assertEquals(itemA.spanX, resultA.spanX)
        assertEquals(itemA.spanY, resultA.spanY)
    }

    @Test
    fun `rollback with multiple items - no overlap and within bounds`() {
        val originalB = GridItem("b", 1, 0, 1, 1)
        val originalC = GridItem("c", 2, 0, 1, 1)
        val currentLayout = listOf(
            GridItem("a", 0, 2, 1, 1),
            GridItem("b", 0, 1, 1, 1),
            GridItem("c", 1, 1, 1, 1)
        )
        val relocatedWithOriginals = mapOf(
            "b" to originalB,
            "c" to originalC
        )
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        assertFalse(ValidationUtils.hasAnyOverlap(result))
        assertTrue(ValidationUtils.allWithinBounds(result, gridSize))
    }

    @Test
    fun `rollback based on current frame occupancy - B original free when A moved away`() {
        val originalB = GridItem("b", 2, 0, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 0, 2, 2, 2),
            GridItem("b", 0, 0, 2, 2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        val resultB = result.find { it.id == "b" }!!
        assertEquals(2, resultB.x)
        assertEquals(0, resultB.y)
    }

    @Test
    fun `rollback based on current frame occupancy - B stays when A still occupies B original`() {
        val originalB = GridItem("b", 1, 1, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 1, 0, 2, 2),
            GridItem("b", 0, 2, 2, 2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)
        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)
        val resultB = result.find { it.id == "b" }!!
        assertEquals(0, resultB.x)
        assertEquals(2, resultB.y)
    }
}
