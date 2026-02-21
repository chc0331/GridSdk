package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.state.OccupancyGrid

/**
 * 드래그 중 재배치된 아이템의 원위치 복귀 가능 여부를 판정합니다.
 *
 * PRD 롤백 규칙: "복귀 판단은 현재 프레임의 점유 상태 기준으로 수행한다"
 */
@InternalApi
internal object RollbackEvaluator {

    /**
     * 현재 레이아웃에서 복귀 가능한 아이템들을 원위치로 되돌린 새 레이아웃을 반환합니다.
     *
     * @param currentLayout 현재 아이템 레이아웃 (드래그 대상이 새 위치에 있음)
     * @param relocatedWithOriginals 재배치된 아이템의 원래 위치 (itemId -> originalItem)
     * @param gridSize 그리드 크기
     * @return 복귀 적용된 레이아웃 (복귀 불가 시 currentLayout과 동일)
     */
    internal fun evaluateRollback(
        currentLayout: List<GridItem>,
        relocatedWithOriginals: Map<String, GridItem>,
        gridSize: GridSize
    ): List<GridItem> {
        if (relocatedWithOriginals.isEmpty()) return currentLayout

        val currentById = currentLayout.associateBy { it.id }
        val toRollback = mutableSetOf<String>()

        for ((itemId, originalItem) in relocatedWithOriginals) {
            val currentItem = currentById[itemId] ?: continue
            // 원래 위치와 다르면 재배치된 상태
            if (currentItem.x != originalItem.x || currentItem.y != originalItem.y) {
                val occupancy = OccupancyGrid.fromItems(gridSize, currentLayout)
                occupancy.removeById(itemId)
                if (occupancy.canPlace(originalItem)) {
                    toRollback.add(itemId)
                }
            }
        }

        if (toRollback.isEmpty()) return currentLayout

        return currentLayout.map { item ->
            if (item.id in toRollback) {
                relocatedWithOriginals[item.id]!!
            } else {
                item
            }
        }
    }
}
