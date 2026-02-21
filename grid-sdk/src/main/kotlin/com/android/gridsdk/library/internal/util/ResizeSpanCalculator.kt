package com.android.gridsdk.library.internal.util

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

/**
 * 드래그 기반 spanX/spanY 변경 계산
 *
 * PRD: "드래그 기반 spanX/spanY 변경 계산 구현"
 *
 * 리사이즈 시 앵커 포인트(기본: 좌상단 x,y)는 고정되고,
 * 드래그 끝 셀 좌표로부터 새로운 span을 계산합니다.
 *
 * - 최소 span: 1
 * - 최대 span: 그리드 경계 내 (x + spanX <= columns, y + spanY <= rows)
 */
@InternalApi
internal object ResizeSpanCalculator {

    /**
     * 셀 단위 드래그 델타로부터 목표 span을 계산하고 제약을 적용합니다.
     *
     * @param item 리사이즈 대상 아이템
     * @param deltaX 셀 단위 x 방향 델타 (양수: 확장, 음수: 축소)
     * @param deltaY 셀 단위 y 방향 델타
     * @param gridSize 그리드 크기
     * @return (clampedSpanX, clampedSpanY)
     */
    internal fun computeSpanFromDelta(
        item: GridItem,
        deltaX: Int,
        deltaY: Int,
        gridSize: GridSize
    ): Pair<Int, Int> {
        val targetSpanX = (item.spanX + deltaX).coerceAtLeast(1)
        val targetSpanY = (item.spanY + deltaY).coerceAtLeast(1)
        return clampSpanForItem(item, targetSpanX, targetSpanY, gridSize)
    }

    /**
     * 드래그 끝 셀 좌표로부터 목표 span을 계산합니다.
     *
     * 앵커는 (item.x, item.y)로 고정.
     * dragEndCellX, dragEndCellY는 드래그가 끝난 셀 (우하단 모서리)입니다.
     *
     * @param item 리사이즈 대상 아이템
     * @param dragEndCellX 드래그 끝 x 셀 (포함)
     * @param dragEndCellY 드래그 끝 y 셀 (포함)
     * @param gridSize 그리드 크기
     * @return (clampedSpanX, clampedSpanY)
     */
    internal fun computeSpanFromDragEnd(
        item: GridItem,
        dragEndCellX: Int,
        dragEndCellY: Int,
        gridSize: GridSize
    ): Pair<Int, Int> {
        val targetSpanX = (dragEndCellX - item.x + 1).coerceAtLeast(1)
        val targetSpanY = (dragEndCellY - item.y + 1).coerceAtLeast(1)
        return clampSpanForItem(item, targetSpanX, targetSpanY, gridSize)
    }

    /**
     * span 최소/최대 제약 적용
     *
     * - 최소: 1
     * - 최대: 그리드 경계 (x + spanX <= columns, y + spanY <= rows)
     *
     * @param item 기준 아이템 (x, y 고정)
     * @param targetSpanX 목표 spanX
     * @param targetSpanY 목표 spanY
     * @param gridSize 그리드 크기
     * @return (clampedSpanX, clampedSpanY)
     */
    internal fun clampSpanForItem(
        item: GridItem,
        targetSpanX: Int,
        targetSpanY: Int,
        gridSize: GridSize
    ): Pair<Int, Int> {
        val minSpanX = 1
        val minSpanY = 1
        val maxSpanX = (gridSize.columns - item.x).coerceAtLeast(1)
        val maxSpanY = (gridSize.rows - item.y).coerceAtLeast(1)

        val clampedX = targetSpanX.coerceIn(minSpanX, maxSpanX)
        val clampedY = targetSpanY.coerceIn(minSpanY, maxSpanY)
        return clampedX to clampedY
    }

    /**
     * 픽셀 드래그 델타를 셀 델타로 변환합니다.
     *
     * @param deltaPx 픽셀 단위 델타
     * @param cellSizePx 셀당 픽셀 크기
     * @return 셀 단위 델타 (반올림)
     */
    internal fun pixelsToCellDelta(deltaPx: Float, cellSizePx: Float): Int {
        if (cellSizePx <= 0f) return 0
        return kotlin.math.round(deltaPx / cellSizePx).toInt()
    }

    /**
     * 리사이즈 중간 프레임 결과 안정화 (깜빡임 방지)
     *
     * 터치 지터로 인한 span 진동(2→3→2→3)을 방지합니다.
     * 이전 span과의 차이가 [hysteresisCells] 미만이면 이전 값을 유지합니다.
     *
     * @param item 리사이즈 대상 아이템
     * @param rawTargetSpanX 드래그로 계산된 원시 spanX
     * @param rawTargetSpanY 드래그로 계산된 원시 spanY
     * @param previousSpanX 이전 프레임의 spanX
     * @param previousSpanY 이전 프레임의 spanY
     * @param gridSize 그리드 크기
     * @param hysteresisCells 히스테리시스 셀 수 (기본 1)
     * @return 안정화된 (spanX, spanY)
     */
    internal fun computeSpanWithHysteresis(
        item: GridItem,
        rawTargetSpanX: Int,
        rawTargetSpanY: Int,
        previousSpanX: Int,
        previousSpanY: Int,
        gridSize: GridSize,
        hysteresisCells: Int = 1
    ): Pair<Int, Int> {
        val (clampedX, clampedY) = clampSpanForItem(item, rawTargetSpanX, rawTargetSpanY, gridSize)
        if (previousSpanX < 1 || previousSpanY < 1) return clampedX to clampedY
        val deltaX = kotlin.math.abs(clampedX - previousSpanX)
        val deltaY = kotlin.math.abs(clampedY - previousSpanY)
        return if (deltaX < hysteresisCells && deltaY < hysteresisCells) {
            previousSpanX to previousSpanY
        } else {
            clampedX to clampedY
        }
    }
}
