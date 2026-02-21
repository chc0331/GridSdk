package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

/**
 * 후보 레이아웃 스코어 계산 및 선정
 *
 * PRD 우선순위:
 * 1. 재배치되는 아이템 수 최소
 * 2. 목표 위치(원래 위치)와의 맨해튼 거리 최소
 * 3. 동률이면 상단-좌측 우선 (행 우선, 열 우선)
 */
@InternalApi
internal object CandidateScorer {

    /**
     * 후보 스코어 (낮을수록 우수)
     *
     * @property relocatedCount 재배치된 아이템 수
     * @property totalManhattanDistance 재배치된 아이템들의 원래 위치 대비 맨해튼 거리 합
     * @property topLeftScore 상단-좌측 우선 점수 (y * columns + x 합계, 낮을수록 상단-좌측)
     */
    internal data class Score(
        val relocatedCount: Int,
        val totalManhattanDistance: Int,
        val topLeftScore: Int
    ) : Comparable<Score> {
        override fun compareTo(other: Score): Int {
            if (relocatedCount != other.relocatedCount) return relocatedCount - other.relocatedCount
            if (totalManhattanDistance != other.totalManhattanDistance) {
                return totalManhattanDistance - other.totalManhattanDistance
            }
            return topLeftScore - other.topLeftScore
        }
    }

    /**
     * 후보의 스코어를 계산합니다.
     *
     * @param relocatedItems 재배치된 아이템 목록 (새 위치)
     * @param originalItemsById 원래 아이템 위치 (id -> GridItem)
     * @param gridSize 그리드 크기 (topLeftScore 계산용)
     * @return 스코어 (낮을수록 우수)
     */
    internal fun score(
        relocatedItems: List<GridItem>,
        originalItemsById: Map<String, GridItem>,
        gridSize: GridSize
    ): Score {
        if (relocatedItems.isEmpty()) {
            return Score(relocatedCount = 0, totalManhattanDistance = 0, topLeftScore = 0)
        }

        var totalDistance = 0
        var topLeft = 0

        for (item in relocatedItems) {
            val original = originalItemsById[item.id]
            if (original != null) {
                totalDistance += manhattanDistance(original.x, original.y, item.x, item.y)
            }
            topLeft += item.y * gridSize.columns + item.x
        }

        return Score(
            relocatedCount = relocatedItems.size,
            totalManhattanDistance = totalDistance,
            topLeftScore = topLeft
        )
    }

    /**
     * 스코어가 적용된 후보 목록에서 최우선 후보 1개를 선정합니다.
     *
     * @param candidates (후보 아이템 목록, 스코어) 쌍의 리스트
     * @return 최우선 후보, 없으면 null
     */
    internal fun selectBest(
        candidates: List<Pair<List<GridItem>, Score>>
    ): List<GridItem>? {
        if (candidates.isEmpty()) return null
        val best = candidates.minByOrNull { it.second }!!
        return best.first
    }

    private fun manhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return kotlin.math.abs(x2 - x1) + kotlin.math.abs(y2 - y1)
    }
}
