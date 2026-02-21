package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class GridEngineRollbackTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `rollback possible when dragged item moves away from relocated item original position`() {
        // A 이동 → B 재배치 → A가 B 원위치에서 벗어남 → B 원위치 복귀
        val originalB = GridItem("b", 1, 1, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 0, 3, 2, 1),  // A at (0,3), does not overlap B's original (1,1)-(3,3)
            GridItem("b", 0, 0, 2, 2)   // B relocated to (0,0)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        val b = result.find { it.id == "b" }!!
        assertEquals(1, b.x)
        assertEquals(1, b.y)
        assertEquals(2, b.spanX)
        assertEquals(2, b.spanY)
    }

    @Test
    fun `rollback not possible when dragged item still occupies relocated item original position`() {
        // A 이동 → B 재배치 → A가 여전히 B 원위치 점유 → B 유지
        val originalB = GridItem("b", 1, 1, 2, 2)
        val currentLayout = listOf(
            GridItem("a", 1, 0, 2, 2),  // A at (1,0) overlaps B's original (1,1)-(3,3)
            GridItem("b", 0, 2, 2, 2)   // B relocated to (0,2)
        )
        val relocatedWithOriginals = mapOf("b" to originalB)

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        val b = result.find { it.id == "b" }!!
        assertEquals(0, b.x)
        assertEquals(2, b.y)
    }

    @Test
    fun `multiple rollbacks when both relocated items original positions become free`() {
        // A 이동 → B, C 재배치 → A 이동으로 B, C 원위치 모두 비어짐 → B, C 모두 복귀
        val originalB = GridItem("b", 1, 0, 1, 1)
        val originalC = GridItem("c", 2, 0, 1, 1)
        val currentLayout = listOf(
            GridItem("a", 0, 2, 1, 1),  // A moved away, B and C originals (1,0), (2,0) are free
            GridItem("b", 0, 1, 1, 1),  // B relocated
            GridItem("c", 1, 1, 1, 1)   // C relocated
        )
        val relocatedWithOriginals = mapOf(
            "b" to originalB,
            "c" to originalC
        )

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        val b = result.find { it.id == "b" }!!
        val c = result.find { it.id == "c" }!!
        assertEquals(1, b.x)
        assertEquals(0, b.y)
        assertEquals(2, c.x)
        assertEquals(0, c.y)
    }

    @Test
    fun `partial rollback when only one relocated item original position is free`() {
        // B, C 재배치 → B만 원위치 비어짐 → B만 복귀, C는 유지
        val originalB = GridItem("b", 0, 0, 1, 1)
        val originalC = GridItem("c", 1, 0, 1, 1)
        val currentLayout = listOf(
            GridItem("a", 1, 0, 1, 1),  // A occupies C's original (1,0)
            GridItem("b", 2, 0, 1, 1),  // B relocated
            GridItem("c", 0, 1, 1, 1)   // C relocated
        )
        val relocatedWithOriginals = mapOf(
            "b" to originalB,
            "c" to originalC
        )

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        val b = result.find { it.id == "b" }!!
        val c = result.find { it.id == "c" }!!
        assertEquals(0, b.x)
        assertEquals(0, b.y)
        assertEquals(0, c.x)
        assertEquals(1, c.y)
    }

    @Test
    fun `empty relocatedWithOriginals returns currentItems unchanged`() {
        val currentLayout = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        val relocatedWithOriginals = emptyMap<String, GridItem>()

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        assertEquals(currentLayout, result)
    }

    @Test
    fun `items at original position are unchanged`() {
        // 재배치 안 된 아이템은 변경 없음
        val currentLayout = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        val relocatedWithOriginals = mapOf(
            "b" to GridItem("b", 1, 0, 1, 1)  // B is already at original
        )

        val result = GridEngine.evaluateRollback(currentLayout, relocatedWithOriginals, gridSize)

        assertEquals(currentLayout, result)
    }

    @Test
    fun `rollback with move flow - A moves away freeing B original`() {
        // A (0,0) 2x2, B (2,0) 2x2. Move A to (2,0) -> B relocated. Then A at (0,2), B original free.
        val initialItems = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2)
        )
        val moveResult = GridEngine.process(
            EngineRequest.move("a", 2, 0, initialItems, gridSize)
        )
        assertTrue(moveResult is EngineResult.Success)
        val success = moveResult as EngineResult.Success
        val afterMove = success.applyTo(initialItems)

        val originalB = initialItems.find { it.id == "b" }!!
        val relocatedWithOriginals = mapOf("b" to originalB)

        // After move: A at (2,0), B relocated. B's original (2,0)-(4,2) is occupied by A.
        // Simulate next frame: A moved to (0,2), B's original now free.
        val currentLayout = listOf(
            GridItem("a", 0, 2, 2, 2),
            afterMove.find { it.id == "b" }!!
        )

        val withRollback = GridEngine.evaluateRollback(
            currentLayout,
            relocatedWithOriginals,
            gridSize
        )

        val b = withRollback.find { it.id == "b" }!!
        assertEquals(2, b.x)
        assertEquals(0, b.y)
    }
}
