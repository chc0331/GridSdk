package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class PlacementExplorerTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `exploreBestCandidate returns layout when no conflict`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 2, 2, 2)
        )
        val target = GridItem("a", 0, 2, 2, 2)

        val result = PlacementExplorer.exploreBestCandidate(items, target, gridSize)

        assertNotNull(result)
        assertEquals(2, result!!.size)
        val targetInResult = result.find { it.id == "a" }
        assertEquals(0, targetInResult!!.x)
        assertEquals(2, targetInResult.y)
        assertEquals(2, targetInResult.spanX)
        assertEquals(2, targetInResult.spanY)
    }

    @Test
    fun `exploreBestCandidate returns null when target out of bounds`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val target = GridItem("a", 5, 5, 1, 1)

        val result = PlacementExplorer.exploreBestCandidate(items, target, gridSize)

        assertNull(result)
    }

    @Test
    fun `exploreBestCandidate relocates single conflicting item`() {
        // a(0,0)를 (1,0)으로 이동 시 b(1,1)과 충돌. b를 (0,2)로 재배치 가능
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 1, 1, 2, 2)
        )
        val target = GridItem("a", 1, 0, 2, 2)

        val result = PlacementExplorer.exploreBestCandidate(items, target, gridSize)

        assertNotNull(result)
        assertEquals(2, result!!.size)
        assertTrue(CandidateValidator.isValidCandidate(result, gridSize))
        val b = result.find { it.id == "b" }!!
        assertFalse(b.x == 1 && b.y == 1)
    }

    @Test
    fun `exploreBestCandidate handles multi-conflict case`() {
        // 5x5 그리드: a(1,1)로 이동 시 b,c,d와 충돌. (0,3), (3,0), (3,3)에 2x2 슬롯 존재
        val largeGrid = GridSize(rows = 5, columns = 5)
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val target = GridItem("a", 1, 1, 2, 2)

        val result = PlacementExplorer.exploreBestCandidate(items, target, largeGrid)

        assertNotNull(result)
        assertTrue(CandidateValidator.isValidCandidate(result!!, largeGrid))
        assertEquals(4, result.size)
    }

    @Test
    fun `exploreBestCandidate prefers fewer relocated items`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1),
            GridItem("c", 2, 0, 1, 1)
        )
        val target = GridItem("a", 1, 0, 1, 1)

        val result = PlacementExplorer.exploreBestCandidate(items, target, gridSize)

        assertNotNull(result)
        // 대상 아이템(a) 제외, 충돌로 인해 재배치된 아이템만 카운트
        val relocatedCount = items.count { orig ->
            orig.id != target.id &&
                run {
                    val placed = result!!.find { it.id == orig.id }!!
                    placed.x != orig.x || placed.y != orig.y
                }
        }
        assertEquals(1, relocatedCount)
    }

    @Test
    fun `findFirstEmptyPosition returns 0,0 for empty grid`() {
        val items = emptyList<GridItem>()
        val pos = PlacementExplorer.findFirstEmptyPosition(items, 1, 1, gridSize)

        assertNotNull(pos)
        assertEquals(0, pos!!.first)
        assertEquals(0, pos.second)
    }

    @Test
    fun `findFirstEmptyPosition returns first slot after occupied area`() {
        val items = listOf(GridItem("a", 0, 0, 2, 2))
        val pos = PlacementExplorer.findFirstEmptyPosition(items, 1, 1, gridSize)

        assertNotNull(pos)
        assertEquals(2, pos!!.first)
        assertEquals(0, pos.second)
    }

    @Test
    fun `findFirstEmptyPosition returns null when grid full`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val pos = PlacementExplorer.findFirstEmptyPosition(items, 1, 1, gridSize)

        assertNull(pos)
    }

    @Test
    fun `exploreAddPosition places item at first empty slot`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        val newItem = GridItem("b", 0, 0, 1, 1)

        val result = PlacementExplorer.exploreAddPosition(items, newItem, gridSize)

        assertNotNull(result)
        assertEquals(1, result!!.x)
        assertEquals(0, result.y)
        assertEquals("b", result.id)
    }

    @Test
    fun `exploreAddPosition returns null when no space`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        val newItem = GridItem("e", 0, 0, 1, 1)

        val result = PlacementExplorer.exploreAddPosition(items, newItem, gridSize)

        assertNull(result)
    }
}
