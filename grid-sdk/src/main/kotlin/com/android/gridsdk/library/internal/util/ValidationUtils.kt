package com.android.gridsdk.library.internal.util

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

/**
 * 그리드 좌표 및 경계 유효성 검사 유틸리티
 *
 * 내부 구현에서 사용되는 유틸리티 함수들입니다.
 */
@InternalApi
internal object ValidationUtils {

    /**
     * 좌표가 음수가 아닌지 확인합니다.
     *
     * @param x x 좌표
     * @param y y 좌표
     * @return 둘 다 0 이상이면 true
     */
    internal fun isNonNegative(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0
    }

    /**
     * Span 값이 유효한지 확인합니다 (1 이상).
     *
     * @param spanX x 방향 span
     * @param spanY y 방향 span
     * @return 둘 다 1 이상이면 true
     */
    internal fun isValidSpan(spanX: Int, spanY: Int): Boolean {
        return spanX > 0 && spanY > 0
    }

    /**
     * 아이템이 그리드 경계를 벗어나지 않는지 확인합니다.
     *
     * @param item 검사할 아이템
     * @param gridSize 그리드 크기
     * @return 경계 내에 있으면 true
     */
    internal fun isWithinBounds(item: GridItem, gridSize: GridSize): Boolean {
        return item.x >= 0 &&
                item.y >= 0 &&
                item.endX <= gridSize.columns &&
                item.endY <= gridSize.rows
    }

    /**
     * 두 아이템이 겹치는지 확인합니다.
     *
     * @param item1 첫 번째 아이템
     * @param item2 두 번째 아이템
     * @return 겹치면 true
     */
    internal fun hasOverlap(item1: GridItem, item2: GridItem): Boolean {
        return item1.overlapsWith(item2)
    }

    /**
     * 아이템 목록에서 특정 아이템과 겹치는 아이템들을 찾습니다.
     *
     * @param item 검사할 아이템
     * @param items 전체 아이템 목록
     * @param excludeItemId 제외할 아이템 ID (자기 자신 제외용)
     * @return 겹치는 아이템 목록
     */
    internal fun findOverlappingItems(
        item: GridItem,
        items: List<GridItem>,
        excludeItemId: String? = null
    ): List<GridItem> {
        return items.filter { other ->
            other.id != excludeItemId &&
                    other.id != item.id &&
                    item.overlapsWith(other)
        }
    }

    /**
     * 아이템 목록에 중복이 있는지 확인합니다 (서로 겹치는 아이템이 있는지).
     *
     * @param items 검사할 아이템 목록
     * @return 겹침이 있으면 true
     */
    internal fun hasAnyOverlap(items: List<GridItem>): Boolean {
        for (i in items.indices) {
            for (j in i + 1 until items.size) {
                if (items[i].overlapsWith(items[j])) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 모든 아이템이 그리드 경계 내에 있는지 확인합니다.
     *
     * @param items 검사할 아이템 목록
     * @param gridSize 그리드 크기
     * @return 모든 아이템이 경계 내에 있으면 true
     */
    internal fun allWithinBounds(items: List<GridItem>, gridSize: GridSize): Boolean {
        return items.all { isWithinBounds(it, gridSize) }
    }

    /**
     * 특정 셀 위치를 차지하는 아이템을 찾습니다.
     *
     * @param x 셀의 x 좌표
     * @param y 셀의 y 좌표
     * @param items 검색 대상 아이템 목록
     * @return 해당 셀을 차지하는 아이템, 없으면 null
     */
    internal fun findItemAtCell(x: Int, y: Int, items: List<GridItem>): GridItem? {
        return items.firstOrNull { it.occupiesCell(x, y) }
    }

    /**
     * 아이템 ID가 고유한지 확인합니다.
     *
     * @param items 검사할 아이템 목록
     * @return 모든 ID가 고유하면 true
     */
    internal fun hasUniqueIds(items: List<GridItem>): Boolean {
        val ids = items.map { it.id }.toSet()
        return ids.size == items.size
    }

    /**
     * 직사각형 영역이 비어있는지 확인합니다.
     *
     * @param x 시작 x 좌표
     * @param y 시작 y 좌표
     * @param spanX x 방향 span
     * @param spanY y 방향 span
     * @param items 현재 배치된 아이템 목록
     * @param excludeItemId 검사에서 제외할 아이템 ID
     * @return 영역이 비어있으면 true
     */
    internal fun isAreaEmpty(
        x: Int,
        y: Int,
        spanX: Int,
        spanY: Int,
        items: List<GridItem>,
        excludeItemId: String? = null
    ): Boolean {
        val testItem = GridItem(
            id = "__test__",
            x = x,
            y = y,
            spanX = spanX,
            spanY = spanY
        )
        return findOverlappingItems(testItem, items, excludeItemId).isEmpty()
    }
}
