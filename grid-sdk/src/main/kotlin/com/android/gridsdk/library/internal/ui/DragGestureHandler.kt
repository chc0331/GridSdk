package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine

/**
 * 드래그 제스처를 Move 요청으로 변환하는 핸들러
 *
 * - detectDragGestures로 드래그 감지
 * - 터치 오프셋 → 셀 좌표 변환 (스냅)
 * - GridEngine.process(EngineRequest.Move) 호출
 */
@InternalApi
internal object DragGestureHandler {

    /**
     * 픽셀 오프셋을 그리드 셀 좌표로 변환합니다.
     * 스냅 규칙: 셀 중심이 아닌 셀 경계 기준 (floor)
     */
    internal fun offsetToCell(
        offset: Offset,
        cellWidthPx: Float,
        cellHeightPx: Float,
        gridSize: GridSize
    ): Pair<Int, Int> {
        if (cellWidthPx <= 0f || cellHeightPx <= 0f) return 0 to 0
        val cellX = (offset.x / cellWidthPx).toInt().coerceIn(0, gridSize.columns - 1)
        val cellY = (offset.y / cellHeightPx).toInt().coerceIn(0, gridSize.rows - 1)
        return cellX to cellY
    }

    /**
     * 아이템에 드래그(이동) 제스처를 연결하는 Modifier
     */
    internal fun Modifier.dragGesture(
        item: GridItem,
        items: List<GridItem>,
        gridSize: GridSize,
        cellWidthPx: Float,
        cellHeightPx: Float,
        bridge: EngineStateBridge
    ): Modifier = pointerInput(item.id, items, gridSize, cellWidthPx, cellHeightPx) {
        detectDragGestures(
            onDragEnd = { bridge.clearTracker() },
            onDragCancel = { bridge.clearTracker() }
        ) { change, _ ->
            change.consume()
            val itemOffsetPx = Offset(
                item.x * cellWidthPx,
                item.y * cellHeightPx
            )
            val gridPos = change.position + itemOffsetPx
            val (targetX, targetY) = offsetToCell(
                gridPos,
                cellWidthPx,
                cellHeightPx,
                gridSize
            )
            if (targetX == item.x && targetY == item.y) return@detectDragGestures
            val request = EngineRequest.Move(
                itemId = item.id,
                targetX = targetX,
                targetY = targetY,
                items = items,
                gridSize = gridSize
            )
            when (val result = GridEngine.process(request)) {
                is EngineResult.Success -> bridge.applySuccess(result, items, gridSize)
                is EngineResult.Failure -> bridge.applyFailure(result)
            }
        }
    }
}
