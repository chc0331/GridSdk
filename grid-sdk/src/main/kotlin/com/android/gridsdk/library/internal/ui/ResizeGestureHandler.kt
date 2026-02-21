package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.State
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
 * 리사이즈 핸들러 제스처를 Resize 요청으로 변환하는 핸들러
 *
 * - 롱프레스 후 표시되는 핸들러에 detectDragGestures 적용
 * - 매 드래그마다 onPreviewSpanChange로 프리뷰 span 전달 (외곽선 실시간 업데이트)
 * - span이 실제로 변경될 때만 GridEngine.process(Resize) 호출
 */
@InternalApi
internal object ResizeGestureHandler {

    /**
     * 리사이즈 핸들러에 드래그 제스처를 연결하는 Modifier.
     *
     * @param cornerHandleSizePx 핸들러 크기 (px, 28.dp)
     * @param overlaySizeState 오버레이 현재 크기 (px) - pointerInput key에서 제외해 드래그 중 제스처 취소 방지
     * @param handleOffsetInItemPx 오버레이 좌상단 기준 핸들러 좌상단 오프셋 (px)
     * @param onPreviewSpanChange 매 드래그마다 (previewSpanX, previewSpanY) 호출. onDragEnd/onDragCancel 시 null 호출
     * @param onPreviewSizePxChange 매 드래그마다 (widthPx, heightPx) 호출. 외곽선 실시간 픽셀 크기. onDragEnd/onDragCancel 시 null 호출
     */
    internal fun Modifier.resizeHandleGesture(
        item: GridItem,
        itemsState: State<List<GridItem>>,
        gridSize: GridSize,
        cellWidthPx: Float,
        cellHeightPx: Float,
        cornerHandleSizePx: Float,
        overlaySizeState: State<Pair<Float, Float>>,
        handleOffsetInItemPx: Offset,
        bridge: EngineStateBridge,
        previousSpanX: Int,
        previousSpanY: Int,
        onPreviewSpanChange: (Pair<Int, Int>?) -> Unit,
        onPreviewSizePxChange: (Pair<Float, Float>?) -> Unit
    ): Modifier = pointerInput(
        item.id,
        gridSize,
        cellWidthPx,
        cellHeightPx,
        cornerHandleSizePx
    ) {
        var lastSpanX = previousSpanX
        var lastSpanY = previousSpanY
        var lastDisplaySpanX = previousSpanX
        var lastDisplaySpanY = previousSpanY
        detectDragGestures(
            onDragEnd = {
                onPreviewSpanChange(null)
                onPreviewSizePxChange(null)
                bridge.clearTracker()
            },
            onDragCancel = {
                onPreviewSpanChange(null)
                onPreviewSizePxChange(null)
                bridge.clearTracker()
            }
        ) { change, _ ->
            change.consume()
            val items = itemsState.value
            val (currentOverlayWidthPx, currentOverlayHeightPx) = overlaySizeState.value
            val currentItem = items.find { it.id == item.id } ?: item
            val itemOffsetPx = Offset(
                currentItem.x * cellWidthPx,
                currentItem.y * cellHeightPx
            )
            val currentHandleOffsetPx = Offset(
                currentOverlayWidthPx - cornerHandleSizePx,
                currentOverlayHeightPx - cornerHandleSizePx
            )
            val gridPos = change.position + itemOffsetPx + currentHandleOffsetPx
            val previewWidthPx = (gridPos.x - itemOffsetPx.x)
                .coerceIn(cellWidthPx, (gridSize.columns - currentItem.x) * cellWidthPx)
            val previewHeightPx = (gridPos.y - itemOffsetPx.y)
                .coerceIn(cellHeightPx, (gridSize.rows - currentItem.y) * cellHeightPx)
            onPreviewSizePxChange(previewWidthPx to previewHeightPx)
            // 셀 중간(50%)을 넘어야 다음 셀로 인정 (의도적인 드래그 구분)
            val dragEndCellX = ((gridPos.x - 0.5f * cellWidthPx) / cellWidthPx).toInt()
                .coerceIn(currentItem.x, gridSize.columns - 1)
            val dragEndCellY = ((gridPos.y - 0.5f * cellHeightPx) / cellHeightPx).toInt()
                .coerceIn(currentItem.y, gridSize.rows - 1)
            val (rawSpanX, rawSpanY) = ResizeSpanCalculator.computeSpanFromDragEnd(
                currentItem,
                dragEndCellX,
                dragEndCellY,
                gridSize
            )
            onPreviewSpanChange(rawSpanX to rawSpanY)
            lastDisplaySpanX = rawSpanX
            lastDisplaySpanY = rawSpanY
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
            if (targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
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
