package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class CandidateScorerTest {

    private val gridSize = GridSize(rows = 4, columns = 4)

    @Test
    fun `score returns zero for empty relocated items`() {
        val score = CandidateScorer.score(emptyList(), emptyMap(), gridSize)
        assertEquals(0, score.relocatedCount)
        assertEquals(0, score.totalManhattanDistance)
        assertEquals(0, score.topLeftScore)
    }

    @Test
    fun `score prefers fewer relocated items`() {
        val originalById = mapOf(
            "a" to GridItem("a", 1, 1, 1, 1)
        )
        val oneRelocated = listOf(GridItem("a", 0, 0, 1, 1))
        val score1 = CandidateScorer.score(oneRelocated, originalById, gridSize)

        val twoRelocated = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 2, 2, 1, 1)
        )
        val originalById2 = originalById + ("b" to GridItem("b", 3, 3, 1, 1))
        val score2 = CandidateScorer.score(twoRelocated, originalById2, gridSize)

        assertTrue(score1 < score2)
    }

    @Test
    fun `score prefers smaller Manhattan distance`() {
        val originalById = mapOf("a" to GridItem("a", 1, 1, 1, 1))
        val closer = listOf(GridItem("a", 0, 1, 1, 1))
        val farther = listOf(GridItem("a", 3, 3, 1, 1))

        val scoreCloser = CandidateScorer.score(closer, originalById, gridSize)
        val scoreFarther = CandidateScorer.score(farther, originalById, gridSize)

        assertTrue(scoreCloser.totalManhattanDistance < scoreFarther.totalManhattanDistance)
        assertTrue(scoreCloser < scoreFarther)
    }

    @Test
    fun `score prefers top-left when distance ties`() {
        // 원래 (2,2)에서 맨해튼 거리 2인 위치들: (1,1), (1,3), (3,1), (3,3)
        val originalById = mapOf("a" to GridItem("a", 2, 2, 1, 1))
        val topLeft = listOf(GridItem("a", 1, 1, 1, 1))      // topLeftScore = 1*4+1 = 5
        val bottomRight = listOf(GridItem("a", 3, 3, 1, 1))  // topLeftScore = 3*4+3 = 15

        val scoreTopLeft = CandidateScorer.score(topLeft, originalById, gridSize)
        val scoreBottomRight = CandidateScorer.score(bottomRight, originalById, gridSize)

        assertEquals(scoreTopLeft.relocatedCount, scoreBottomRight.relocatedCount)
        assertEquals(scoreTopLeft.totalManhattanDistance, scoreBottomRight.totalManhattanDistance)
        assertTrue(scoreTopLeft.topLeftScore < scoreBottomRight.topLeftScore)
        assertTrue(scoreTopLeft < scoreBottomRight)
    }

    @Test
    fun `selectBest returns null for empty list`() {
        assertNull(CandidateScorer.selectBest(emptyList()))
    }

    @Test
    fun `selectBest returns single candidate when only one`() {
        val candidate = listOf(GridItem("a", 0, 0, 1, 1))
        val score = CandidateScorer.Score(0, 0, 0)
        val result = CandidateScorer.selectBest(listOf(candidate to score))
        assertEquals(candidate, result)
    }

    @Test
    fun `selectBest returns best of multiple candidates`() {
        val worse = listOf(GridItem("a", 3, 3, 1, 1))
        val better = listOf(GridItem("a", 0, 0, 1, 1))
        val originalById = mapOf("a" to GridItem("a", 1, 1, 1, 1))

        val candidates = listOf(
            worse to CandidateScorer.score(worse, originalById, gridSize),
            better to CandidateScorer.score(better, originalById, gridSize)
        )
        val result = CandidateScorer.selectBest(candidates)
        assertEquals(better, result)
    }
}
