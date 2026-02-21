package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class CandidateValidatorTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `isValidCandidate returns true for empty list`() {
        assertTrue(CandidateValidator.isValidCandidate(emptyList(), gridSize))
    }

    @Test
    fun `isValidCandidate returns true for valid single item`() {
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        assertTrue(CandidateValidator.isValidCandidate(items, gridSize))
    }

    @Test
    fun `isValidCandidate returns true for valid non-overlapping items`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
        assertTrue(CandidateValidator.isValidCandidate(items, gridSize))
    }

    @Test
    fun `isValidCandidate returns false for out of bounds`() {
        val items = listOf(GridItem("a", 3, 3, 2, 2))
        assertFalse(CandidateValidator.isValidCandidate(items, gridSize))
    }

    @Test
    fun `isValidCandidate returns false for overlapping items`() {
        val items = listOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 1, 1, 2, 2)
        )
        assertFalse(CandidateValidator.isValidCandidate(items, gridSize))
    }

    @Test
    fun `isValidCandidate returns false when any item exceeds bounds`() {
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 0, 4, 1, 1)
        )
        assertFalse(CandidateValidator.isValidCandidate(items, gridSize))
    }
}
