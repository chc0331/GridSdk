package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.state.OccupancyGrid
import com.android.gridsdk.library.internal.util.ValidationUtils

/**
 * 후보 배치 상태 탐색
 *
 * 대상 아이템을 목표 위치에 두고, 충돌 아이템들의 재배치 후보를 탐색합니다.
 * 상단-좌측 빈 공간 탐색으로 충돌 아이템 배치를 시도하고,
 * 유효 후보 중 스코어가 가장 우수한 1개를 반환합니다.
 */
@InternalApi
internal object PlacementExplorer {

    /**
     * 대상 아이템을 목표 위치에 두고 충돌을 해소할 수 있는 최적의 후보 레이아웃을 탐색합니다.
     *
     * @param items 현재 아이템 목록
     * @param targetItem 목표 위치/크기의 대상 아이템 (items에 동일 id가 있어야 함)
     * @param gridSize 그리드 크기
     * @return 최적 후보 레이아웃, 유효 후보가 없으면 null
     */
    internal fun exploreBestCandidate(
        items: List<GridItem>,
        targetItem: GridItem,
        gridSize: GridSize
    ): List<GridItem>? {
        if (!targetItem.isValidIn(gridSize)) return null

        val originalItemsById = items.associateBy { it.id }
        val itemsWithoutTarget = items.filter { it.id != targetItem.id }

        // 대상 아이템을 제외한 점유 상태에 대상 아이템을 목표 위치에 배치
        val occupancy = OccupancyGrid.fromItems(gridSize, itemsWithoutTarget)
        val conflicts = occupancy.getConflictingItems(targetItem)

        if (conflicts.isEmpty()) {
            // 무충돌: 대상만 이동/리사이즈
            val candidate = itemsWithoutTarget + targetItem
            return if (CandidateValidator.isValidCandidate(candidate, gridSize)) candidate else null
        }

        // 충돌 아이템들 재배치 후보 탐색
        val conflictingItems = items.filter { it.id in conflicts }
        val fixedItems = itemsWithoutTarget - conflictingItems
        val basePlaced = fixedItems + targetItem

        val candidates = explorePlacements(
            placedItems = basePlaced,
            remainingToPlace = conflictingItems,
            gridSize = gridSize
        )

        if (candidates.isEmpty()) return null

        val scored = candidates.map { candidate ->
            val relocatedItems = conflictingItems.map { orig ->
                candidate.find { it.id == orig.id } ?: orig
            }
            val score = CandidateScorer.score(relocatedItems, originalItemsById, gridSize)
            candidate to score
        }

        return CandidateScorer.selectBest(scored)
    }

    /**
     * 상단-좌측(행 우선, 열 우선) 순으로 첫 번째 배치 가능한 빈 위치를 반환합니다.
     *
     * Add 시나리오에서 사용됩니다.
     *
     * @param items 현재 배치된 아이템 목록
     * @param spanX 배치할 영역의 열 방향 span
     * @param spanY 배치할 영역의 행 방향 span
     * @param gridSize 그리드 크기
     * @return 첫 번째 유효 위치 (x, y), 없으면 null
     */
    internal fun findFirstEmptyPosition(
        items: List<GridItem>,
        spanX: Int,
        spanY: Int,
        gridSize: GridSize
    ): Pair<Int, Int>? {
        return findTopLeftEmptyPositions(items, spanX, spanY, gridSize).firstOrNull()
    }

    /**
     * 새 아이템을 상단-좌측 첫 빈 공간에 배치합니다.
     *
     * @param items 현재 아이템 목록
     * @param item 추가할 아이템 (id, spanX, spanY 사용, x/y는 무시)
     * @param gridSize 그리드 크기
     * @return 배치된 아이템, 유효 위치가 없으면 null
     */
    internal fun exploreAddPosition(
        items: List<GridItem>,
        item: GridItem,
        gridSize: GridSize
    ): GridItem? {
        val position = findFirstEmptyPosition(items, item.spanX, item.spanY, gridSize)
            ?: return null
        return item.moveTo(position.first, position.second)
    }

    /**
     * 남은 아이템들을 배치할 수 있는 모든 유효한 레이아웃을 탐색합니다.
     */
    private fun explorePlacements(
        placedItems: List<GridItem>,
        remainingToPlace: List<GridItem>,
        gridSize: GridSize
    ): List<List<GridItem>> {
        if (remainingToPlace.isEmpty()) {
            return if (CandidateValidator.isValidCandidate(placedItems, gridSize)) {
                listOf(placedItems)
            } else {
                emptyList()
            }
        }

        val item = remainingToPlace.first()
        val rest = remainingToPlace.drop(1)
        val positions = findTopLeftEmptyPositions(placedItems, item.spanX, item.spanY, gridSize)

        return positions.flatMap { (x, y) ->
            val newItem = item.moveTo(x, y)
            val newPlaced = placedItems + newItem
            explorePlacements(newPlaced, rest, gridSize)
        }
    }

    /**
     * 상단-좌측(행 우선, 열 우선) 순으로 배치 가능한 빈 위치를 모두 반환합니다.
     */
    private fun findTopLeftEmptyPositions(
        placedItems: List<GridItem>,
        spanX: Int,
        spanY: Int,
        gridSize: GridSize
    ): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until gridSize.rows) {
            for (x in 0 until gridSize.columns) {
                if (x + spanX <= gridSize.columns && y + spanY <= gridSize.rows &&
                    ValidationUtils.isAreaEmpty(x, y, spanX, spanY, placedItems, null)
                ) {
                    result.add(x to y)
                }
            }
        }
        return result
    }
}
