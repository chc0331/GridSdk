package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.internal.util.ResizeSpanCalculator
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine

/**
 * Long press + drag 제스처를 Resize 요청으로 변환하는 핸들러
 *
 * - detectDragGesturesAfterLongPress로 롱프레스 후 드래그 감지
 * - ResizeSpanCalculator.computeSpanFromDragEnd로 span 계산
 * - computeSpanWithHysteresis로 깜빡임 방지
 * - GridEngine.process(EngineRequest.Resize) 호출
 */
@InternalApi
internal object ResizeGestureHandler {

    /**
     * 아이템에 리사이즈(long press + drag) 제스처를 연결하는 Modifier
     */
    internal fun Modifier.resizeGesture(
        item: GridItem,
        items: List<GridItem>,
        gridSize: GridSize,
        cellWidthPx: Float,
        cellHeightPx: Float,
        bridge: EngineStateBridge,
        previousSpanX: Int,
        previousSpanY: Int
    ): Modifier = pointerInput(
        item.id,
        items,
        gridSize,
        cellWidthPx,
        cellHeightPx,
        previousSpanX,
        previousSpanY
    ) {
        var lastSpanX = previousSpanX
        var lastSpanY = previousSpanY
        detectDragGesturesAfterLongPress(
            onDragEnd = { bridge.clearTracker() },
            onDragCancel = { bridge.clearTracker() }
        ) { change, _ ->
            change.consume()
            val currentItem = items.find { it.id == item.id } ?: item
            val itemOffsetPx = Offset(
                currentItem.x * cellWidthPx,
                currentItem.y * cellHeightPx
            )
            val gridPos = change.position + itemOffsetPx
            val dragEndCellX = (gridPos.x / cellWidthPx).toInt()
                .coerceIn(currentItem.x, gridSize.columns - 1)
            val dragEndCellY = (gridPos.y / cellHeightPx).toInt()
                .coerceIn(currentItem.y, gridSize.rows - 1)
            val (rawSpanX, rawSpanY) = ResizeSpanCalculator.computeSpanFromDragEnd(
                currentItem,
                dragEndCellX,
                dragEndCellY,
                gridSize
            )
            val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                currentItem,
                rawSpanX,
                rawSpanY,
                lastSpanX,
                lastSpanY,
                gridSize,
                hysteresisCells = 1
            )
            lastSpanX = targetSpanX
            lastSpanY = targetSpanY
            if (targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGesturesAfterLongPress
            val request = EngineRequest.Resize(
                itemId = item.id,
                targetSpanX = targetSpanX,
                targetSpanY = targetSpanY,
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
