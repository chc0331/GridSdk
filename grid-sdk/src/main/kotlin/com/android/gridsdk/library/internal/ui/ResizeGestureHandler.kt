package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.internal.util.ResizeCorner
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
 * - topLeft/topRight/bottomLeft는 고정 쪽(right 또는 bottom) 유지하며 Move 후 Resize 호출
 */
@InternalApi
internal object ResizeGestureHandler {

    /**
     * 리사이즈 핸들러에 드래그 제스처를 연결하는 Modifier.
     *
     * @param resizeCorner 핸들 코너 (고정 쪽 및 계산 방식 결정)
     * @param cornerHandleSizePx 핸들러 크기 (px, 28.dp)
     * @param overlaySizeState 오버레이 현재 크기 (px)
     * @param onPreviewOffsetPxChange 코너가 topLeft/topRight/bottomLeft일 때만 (offsetXPx, offsetYPx) 전달. onDragEnd/onDragCancel 시 null
     * @param onPreviewSpanChange 매 드래그마다 (previewSpanX, previewSpanY) 호출. onDragEnd/onDragCancel 시 null 호출
     * @param onPreviewSizePxChange 매 드래그마다 (widthPx, heightPx) 호출. onDragEnd/onDragCancel 시 null 호출
     */
    internal fun Modifier.resizeHandleGesture(
        item: GridItem,
        itemsState: State<List<GridItem>>,
        gridSize: GridSize,
        cellWidthPx: Float,
        cellHeightPx: Float,
        cornerHandleSizePx: Float,
        overlaySizeState: State<Pair<Float, Float>>,
        resizeCorner: ResizeCorner,
        bridge: EngineStateBridge,
        previousSpanX: Int,
        previousSpanY: Int,
        onPreviewSpanChange: (Pair<Int, Int>?) -> Unit,
        onPreviewSizePxChange: (Pair<Float, Float>?) -> Unit,
        onPreviewOffsetPxChange: (Pair<Float, Float>?) -> Unit = {}
    ): Modifier = pointerInput(
        item.id,
        resizeCorner,
        gridSize,
        cellWidthPx,
        cellHeightPx,
        cornerHandleSizePx
    ) {
        var lastSpanX = previousSpanX
        var lastSpanY = previousSpanY
        var lastDisplaySpanX = previousSpanX
        var lastDisplaySpanY = previousSpanY
        var lastPreviewOffsetPx: Pair<Float, Float>? = null
        var lastPreviewSizePx: Pair<Float, Float>? = null
        // 드래그 시작 시점의 고정 끝 셀 인덱스 스냅샷 (Move로 currentItem이 변경돼도 기준점 유지)
        var dragStartFixedRight = -1
        var dragStartFixedBottom = -1
        // 드래그 시작 시점의 아이템 절대 픽셀 위치 스냅샷 (effectiveOffsetPx와 독립적으로 dragEndCell 계산)
        var dragStartItemOffsetXPx = -1f
        var dragStartItemOffsetYPx = -1f
        detectDragGestures(
            onDragEnd = {
                lastPreviewOffsetPx = null
                lastPreviewSizePx = null
                dragStartFixedRight = -1
                dragStartFixedBottom = -1
                dragStartItemOffsetXPx = -1f
                dragStartItemOffsetYPx = -1f
                onPreviewSpanChange(null)
                onPreviewSizePxChange(null)
                onPreviewOffsetPxChange(null)
                bridge.clearTracker()
            },
            onDragCancel = {
                lastPreviewOffsetPx = null
                lastPreviewSizePx = null
                dragStartFixedRight = -1
                dragStartFixedBottom = -1
                dragStartItemOffsetXPx = -1f
                dragStartItemOffsetYPx = -1f
                onPreviewSpanChange(null)
                onPreviewSizePxChange(null)
                onPreviewOffsetPxChange(null)
                bridge.clearTracker()
            }
        ) { change, _ ->
            change.consume()
            val items = itemsState.value
            val currentItem = items.find { it.id == item.id } ?: item
            val itemOffsetPx = Offset(
                currentItem.x * cellWidthPx,
                currentItem.y * cellHeightPx
            )
            val defaultSizePx = currentItem.spanX * cellWidthPx to currentItem.spanY * cellHeightPx
            val defaultOffsetPx = itemOffsetPx.x to itemOffsetPx.y
            val effectiveOffsetPx = lastPreviewOffsetPx ?: defaultOffsetPx
            val effectiveSizePx = lastPreviewSizePx ?: defaultSizePx
            val currentHandleOffsetPx = when (resizeCorner) {
                ResizeCorner.TopStart -> Offset(0f, 0f)
                ResizeCorner.TopEnd -> Offset(
                    effectiveSizePx.first - cornerHandleSizePx,
                    0f
                )
                ResizeCorner.BottomStart -> Offset(
                    0f,
                    effectiveSizePx.second - cornerHandleSizePx
                )
                ResizeCorner.BottomEnd -> Offset(
                    effectiveSizePx.first - cornerHandleSizePx,
                    effectiveSizePx.second - cornerHandleSizePx
                )
            }
            val gridPos = change.position + Offset(effectiveOffsetPx.first, effectiveOffsetPx.second) + currentHandleOffsetPx
            // 셀 중간(50%)을 넘어야 다음 셀로 인정
            val dragEndCellX = ((gridPos.x - 0.5f * cellWidthPx) / cellWidthPx).toInt()
                .coerceIn(0, gridSize.columns - 1)
            val dragEndCellY = ((gridPos.y - 0.5f * cellHeightPx) / cellHeightPx).toInt()
                .coerceIn(0, gridSize.rows - 1)

            when (resizeCorner) {
                ResizeCorner.BottomEnd -> {
                    val previewWidthPx = (gridPos.x - itemOffsetPx.x)
                        .coerceIn(cellWidthPx, (gridSize.columns - currentItem.x) * cellWidthPx)
                    val previewHeightPx = (gridPos.y - itemOffsetPx.y)
                        .coerceIn(cellHeightPx, (gridSize.rows - currentItem.y) * cellHeightPx)
                    val previewSizePx = previewWidthPx to previewHeightPx
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewSizePx = previewSizePx
                    val dragEndX = dragEndCellX.coerceIn(currentItem.x, gridSize.columns - 1)
                    val dragEndY = dragEndCellY.coerceIn(currentItem.y, gridSize.rows - 1)
                    val (rawSpanX, rawSpanY) = ResizeSpanCalculator.computeSpanFromDragEnd(
                        currentItem, dragEndX, dragEndY, gridSize
                    )
                    onPreviewSpanChange(rawSpanX to rawSpanY)
                    lastDisplaySpanX = rawSpanX
                    lastDisplaySpanY = rawSpanY
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, rawSpanX, rawSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    when (val result = GridEngine.process(EngineRequest.resize(item.id, targetSpanX, targetSpanY, items, gridSize))) {
                        is EngineResult.Success -> bridge.applySuccess(result, items, gridSize)
                        is EngineResult.Failure -> bridge.applyFailure(result)
                    }
                }
                ResizeCorner.TopStart -> {
                    // 드래그 시작 시점의 fixedRight/Bottom을 한 번만 캡처 (Move 후 currentItem 변경에 영향받지 않음)
                    if (dragStartFixedRight < 0) dragStartFixedRight = currentItem.x + currentItem.spanX - 1
                    if (dragStartFixedBottom < 0) dragStartFixedBottom = currentItem.y + currentItem.spanY - 1
                    val fixedRight = dragStartFixedRight
                    val fixedBottom = dragStartFixedBottom
                    val fixedRightPx = (fixedRight + 1) * cellWidthPx
                    val fixedBottomPx = (fixedBottom + 1) * cellHeightPx
                    // 최소 1셀 유지: 상한은 고정 끝 - 1셀
                    val offsetXPx = gridPos.x.coerceIn(0f, fixedRightPx - cellWidthPx)
                    val offsetYPx = gridPos.y.coerceIn(0f, fixedBottomPx - cellHeightPx)
                    // 프리뷰 크기 상한: 그리드 좌/상단까지 확장 가능 (fixedRightPx / fixedBottomPx 기준)
                    val previewWidthPx = (fixedRightPx - offsetXPx).coerceIn(
                        cellWidthPx, fixedRightPx
                    )
                    val previewHeightPx = (fixedBottomPx - offsetYPx).coerceIn(
                        cellHeightPx, fixedBottomPx
                    )
                    val previewOffsetPx = offsetXPx to offsetYPx
                    val previewSizePx = previewWidthPx to previewHeightPx
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    var newX = dragEndCellX.coerceIn(0, fixedRight)
                    var newY = dragEndCellY.coerceIn(0, fixedBottom)
                    var newSpanX = (fixedRight - newX + 1).coerceAtLeast(1)
                    var newSpanY = (fixedBottom - newY + 1).coerceAtLeast(1)
                    val clampResult = ResizeSpanCalculator.clampResizeResult(
                        newX, newY, newSpanX, newSpanY, gridSize
                    )
                    val (clampedX, clampedY) = clampResult.first
                    val (clampedSpanX, clampedSpanY) = clampResult.second
                    newX = clampedX
                    newY = clampedY
                    newSpanX = clampedSpanX
                    newSpanY = clampedSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    // BottomEnd와 동일하게 hysteresis 적용
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newX == currentItem.x && newY == currentItem.y && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val moveResult = GridEngine.process(EngineRequest.move(item.id, newX, newY, items, gridSize))
                    if (moveResult is EngineResult.Failure) {
                        bridge.applyFailure(moveResult)
                        return@detectDragGestures
                    }
                    val newItems = (moveResult as EngineResult.Success).applyTo(items)
                    when (val resizeResult = GridEngine.process(EngineRequest.resize(item.id, targetSpanX, targetSpanY, newItems, gridSize))) {
                        is EngineResult.Success -> {
                            bridge.applySuccessBatched(moveResult, resizeResult, items, gridSize, item.id)
                            lastPreviewOffsetPx = newX * cellWidthPx to newY * cellHeightPx
                            lastPreviewSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                        }
                        is EngineResult.Failure -> {
                            bridge.applySuccess(moveResult, items, gridSize)
                            bridge.applyFailure(resizeResult)
                        }
                    }
                }
                ResizeCorner.TopEnd -> {
                    // 드래그 시작 시점의 fixedBottom을 한 번만 캡처 (Move 후 currentItem 변경에 영향받지 않음)
                    if (dragStartFixedBottom < 0) dragStartFixedBottom = currentItem.y + currentItem.spanY - 1
                    val fixedBottom = dragStartFixedBottom
                    val fixedBottomPx = (fixedBottom + 1) * cellHeightPx
                    val offsetXPx = itemOffsetPx.x
                    // 최소 1셀 유지: 상한은 고정 끝 - 1셀
                    val offsetYPx = gridPos.y.coerceIn(0f, fixedBottomPx - cellHeightPx)
                    // previewWidthPx 상한: BottomEnd와 동일하게 그리드 경계 기준
                    val previewWidthPx = (gridPos.x - itemOffsetPx.x).coerceIn(
                        cellWidthPx, (gridSize.columns - currentItem.x) * cellWidthPx
                    )
                    // previewHeightPx 상한: 그리드 상단까지 확장 가능 (fixedBottomPx 기준)
                    val previewHeightPx = (fixedBottomPx - offsetYPx).coerceIn(
                        cellHeightPx, fixedBottomPx
                    )
                    val previewOffsetPx = offsetXPx to offsetYPx
                    val previewSizePx = previewWidthPx to previewHeightPx
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    val dragEndX = dragEndCellX.coerceIn(currentItem.x, gridSize.columns - 1)
                    var newY = dragEndCellY.coerceIn(0, fixedBottom)
                    var newSpanX = (dragEndX - currentItem.x + 1).coerceAtLeast(1)
                    var newSpanY = (fixedBottom - newY + 1).coerceAtLeast(1)
                    val clampResult = ResizeSpanCalculator.clampResizeResult(
                        currentItem.x, newY, newSpanX, newSpanY, gridSize
                    )
                    val (_, clampedY) = clampResult.first
                    val (clampedSpanX, clampedSpanY) = clampResult.second
                    newY = clampedY
                    newSpanX = clampedSpanX
                    newSpanY = clampedSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    // BottomEnd와 동일하게 hysteresis 적용
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newY == currentItem.y && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val moveResult = GridEngine.process(EngineRequest.move(item.id, currentItem.x, newY, items, gridSize))
                    if (moveResult is EngineResult.Failure) {
                        bridge.applyFailure(moveResult)
                        return@detectDragGestures
                    }
                    val newItems = (moveResult as EngineResult.Success).applyTo(items)
                    when (val resizeResult = GridEngine.process(EngineRequest.resize(item.id, targetSpanX, targetSpanY, newItems, gridSize))) {
                        is EngineResult.Success -> {
                            bridge.applySuccessBatched(moveResult, resizeResult, items, gridSize, item.id)
                            lastPreviewOffsetPx = currentItem.x * cellWidthPx to newY * cellHeightPx
                            lastPreviewSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                        }
                        is EngineResult.Failure -> {
                            bridge.applySuccess(moveResult, items, gridSize)
                            bridge.applyFailure(resizeResult)
                        }
                    }
                }
                ResizeCorner.BottomStart -> {
                    // 드래그 시작 시점의 값을 한 번만 캡처
                    if (dragStartFixedRight < 0) dragStartFixedRight = currentItem.x + currentItem.spanX - 1
                    if (dragStartItemOffsetXPx < 0f) dragStartItemOffsetXPx = currentItem.x * cellWidthPx
                    val fixedRight = dragStartFixedRight
                    val fixedRightPx = (fixedRight + 1) * cellWidthPx
                    // X축 dragEndCell: effectiveOffsetPx 독립적으로 재계산 (Move 후 피드백 루프 차단)
                    // 현재 터치 X + 드래그시작 아이템 offsetX = 그리드 절대 x
                    val absGridX = change.position.x + dragStartItemOffsetXPx
                    val dragEndCellXLocal = ((absGridX - 0.5f * cellWidthPx) / cellWidthPx).toInt()
                        .coerceIn(0, gridSize.columns - 1)
                    // 최소 1셀 유지: 상한은 고정 끝 - 1셀
                    val offsetXPx = absGridX.coerceIn(0f, fixedRightPx - cellWidthPx)
                    val offsetYPx = itemOffsetPx.y
                    // previewWidthPx 상한: 그리드 좌측까지 확장 가능 (fixedRightPx 기준)
                    val previewWidthPx = (fixedRightPx - offsetXPx).coerceIn(
                        cellWidthPx, fixedRightPx
                    )
                    // previewHeightPx 상한: BottomEnd와 동일하게 그리드 경계 기준
                    val previewHeightPx = (gridPos.y - itemOffsetPx.y).coerceIn(
                        cellHeightPx, (gridSize.rows - currentItem.y) * cellHeightPx
                    )
                    val previewOffsetPx = offsetXPx to offsetYPx
                    val previewSizePx = previewWidthPx to previewHeightPx
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    val dragEndY = dragEndCellY.coerceIn(currentItem.y, gridSize.rows - 1)
                    var newX = dragEndCellXLocal.coerceIn(0, fixedRight)
                    var newSpanX = (fixedRight - newX + 1).coerceAtLeast(1)
                    var newSpanY = (dragEndY - currentItem.y + 1).coerceAtLeast(1)
                    val clampResult = ResizeSpanCalculator.clampResizeResult(
                        newX, currentItem.y, newSpanX, newSpanY, gridSize
                    )
                    val (clampedX, _) = clampResult.first
                    val (clampedSpanX, clampedSpanY) = clampResult.second
                    newX = clampedX
                    newSpanX = clampedSpanX
                    newSpanY = clampedSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    // BottomEnd와 동일하게 hysteresis 적용
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newX == currentItem.x && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val moveResult = GridEngine.process(EngineRequest.move(item.id, newX, currentItem.y, items, gridSize))
                    if (moveResult is EngineResult.Failure) {
                        bridge.applyFailure(moveResult)
                        return@detectDragGestures
                    }
                    val newItems = (moveResult as EngineResult.Success).applyTo(items)
                    when (val resizeResult = GridEngine.process(EngineRequest.resize(item.id, targetSpanX, targetSpanY, newItems, gridSize))) {
                        is EngineResult.Success -> {
                            bridge.applySuccessBatched(moveResult, resizeResult, items, gridSize, item.id)
                            lastPreviewOffsetPx = newX * cellWidthPx to currentItem.y * cellHeightPx
                            lastPreviewSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                        }
                        is EngineResult.Failure -> {
                            bridge.applySuccess(moveResult, items, gridSize)
                            bridge.applyFailure(resizeResult)
                        }
                    }
                }
            }
        }
    }
}
