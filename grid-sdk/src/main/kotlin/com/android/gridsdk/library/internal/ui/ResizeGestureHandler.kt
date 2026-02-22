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
     * Move 후 Resize를 순차 적용하고, 성공 시 배치 적용 및 프리뷰 오프셋/사이즈 갱신 콜백 호출.
     *
     * @return Move 실패 시 false (호출부에서 return 처리), 그 외 true
     */
    private fun applyMoveAndResize(
        itemId: String,
        newX: Int,
        newY: Int,
        targetSpanX: Int,
        targetSpanY: Int,
        items: List<GridItem>,
        gridSize: GridSize,
        bridge: EngineStateBridge,
        successOffsetPx: Pair<Float, Float>,
        successSizePx: Pair<Float, Float>,
        onBatchedSuccess: (offsetPx: Pair<Float, Float>, sizePx: Pair<Float, Float>) -> Unit
    ): Boolean {
        val moveResult = GridEngine.process(EngineRequest.move(itemId, newX, newY, items, gridSize))
        if (moveResult is EngineResult.Failure) {
            bridge.applyFailure(moveResult)
            return false
        }
        val newItems = (moveResult as EngineResult.Success).applyTo(items)
        when (val resizeResult = GridEngine.process(EngineRequest.resize(itemId, targetSpanX, targetSpanY, newItems, gridSize))) {
            is EngineResult.Success -> {
                bridge.applySuccessBatched(moveResult, resizeResult, items, gridSize, itemId)
                onBatchedSuccess(successOffsetPx, successSizePx)
            }
            is EngineResult.Failure -> {
                bridge.applySuccess(moveResult, items, gridSize)
                bridge.applyFailure(resizeResult)
            }
        }
        return true
    }

    /**
     * 코너별 고정 엣지 스냅샷 값 (이번 프레임에 사용할 값 + 다음 프레임을 위해 갱신할 var 값).
     */
    private data class FixedEdgesSnapshot(
        val fixedRight: Int,
        val fixedBottom: Int,
        val dragStartItemOffsetXPx: Float,
        val newDragStartFixedRight: Int,
        val newDragStartFixedBottom: Int,
        val newDragStartItemOffsetXPx: Float
    )

    /**
     * 코너에 따라 고정 엣지(fixedRight/fixedBottom) 및 드래그 시작 시 아이템 offsetX 스냅샷을 한 곳에서 계산.
     * 호출부에서 newDragStart* 값을 var에 반영하고, fixedRight/fixedBottom/dragStartItemOffsetXPx를 이번 프레임 계산에 사용.
     */
    private fun snapshotFixedEdges(
        resizeCorner: ResizeCorner,
        currentItem: GridItem,
        cellWidthPx: Float,
        currentFixedRight: Int,
        currentFixedBottom: Int,
        currentItemOffsetXPx: Float
    ): FixedEdgesSnapshot = when (resizeCorner) {
        ResizeCorner.BottomEnd -> FixedEdgesSnapshot(-1, -1, -1f, -1, -1, -1f)
        ResizeCorner.TopStart -> {
            val newRight = if (currentFixedRight < 0) currentItem.x + currentItem.spanX - 1 else currentFixedRight
            val newBottom = if (currentFixedBottom < 0) currentItem.y + currentItem.spanY - 1 else currentFixedBottom
            FixedEdgesSnapshot(newRight, newBottom, -1f, newRight, newBottom, -1f)
        }
        ResizeCorner.TopEnd -> {
            val newBottom = if (currentFixedBottom < 0) currentItem.y + currentItem.spanY - 1 else currentFixedBottom
            FixedEdgesSnapshot(-1, newBottom, -1f, -1, newBottom, -1f)
        }
        ResizeCorner.BottomStart -> {
            val newRight = if (currentFixedRight < 0) currentItem.x + currentItem.spanX - 1 else currentFixedRight
            val newOffsetX = if (currentItemOffsetXPx < 0f) currentItem.x * cellWidthPx else currentItemOffsetXPx
            FixedEdgesSnapshot(newRight, -1, newOffsetX, newRight, -1, newOffsetX)
        }
    }

    /**
     * 코너별 프리뷰 오프셋(좌상)과 크기(너비, 높이) px를 계산.
     *
     * @param absGridX BottomStart 전용: 터치 X + 드래그 시작 시 아이템 offsetX (그 외 0f)
     * @return first = (offsetXPx, offsetYPx), second = (widthPx, heightPx)
     */
    private fun computePreviewOffsetAndSize(
        resizeCorner: ResizeCorner,
        gridPos: Offset,
        itemOffsetPx: Offset,
        currentItem: GridItem,
        fixedRightPx: Float,
        fixedBottomPx: Float,
        absGridX: Float,
        cellWidthPx: Float,
        cellHeightPx: Float,
        gridSize: GridSize
    ): Pair<Pair<Float, Float>, Pair<Float, Float>> = when (resizeCorner) {
        ResizeCorner.BottomEnd -> {
            val widthPx = (gridPos.x - itemOffsetPx.x)
                .coerceIn(cellWidthPx, (gridSize.columns - currentItem.x) * cellWidthPx)
            val heightPx = (gridPos.y - itemOffsetPx.y)
                .coerceIn(cellHeightPx, (gridSize.rows - currentItem.y) * cellHeightPx)
            (itemOffsetPx.x to itemOffsetPx.y) to (widthPx to heightPx)
        }
        ResizeCorner.TopStart -> {
            val offsetXPx = gridPos.x.coerceIn(0f, fixedRightPx - cellWidthPx)
            val offsetYPx = gridPos.y.coerceIn(0f, fixedBottomPx - cellHeightPx)
            val widthPx = (fixedRightPx - offsetXPx).coerceIn(cellWidthPx, fixedRightPx)
            val heightPx = (fixedBottomPx - offsetYPx).coerceIn(cellHeightPx, fixedBottomPx)
            (offsetXPx to offsetYPx) to (widthPx to heightPx)
        }
        ResizeCorner.TopEnd -> {
            val offsetXPx = itemOffsetPx.x
            val offsetYPx = gridPos.y.coerceIn(0f, fixedBottomPx - cellHeightPx)
            val widthPx = (gridPos.x - itemOffsetPx.x).coerceIn(
                cellWidthPx, (gridSize.columns - currentItem.x) * cellWidthPx
            )
            val heightPx = (fixedBottomPx - offsetYPx).coerceIn(cellHeightPx, fixedBottomPx)
            (offsetXPx to offsetYPx) to (widthPx to heightPx)
        }
        ResizeCorner.BottomStart -> {
            val offsetXPx = absGridX.coerceIn(0f, fixedRightPx - cellWidthPx)
            val offsetYPx = itemOffsetPx.y
            val widthPx = (fixedRightPx - offsetXPx).coerceIn(cellWidthPx, fixedRightPx)
            val heightPx = (gridPos.y - itemOffsetPx.y).coerceIn(
                cellHeightPx, (gridSize.rows - currentItem.y) * cellHeightPx
            )
            (offsetXPx to offsetYPx) to (widthPx to heightPx)
        }
    }

    /**
     * Move+Resize 코너(TopStart, TopEnd, BottomStart)에서 드래그 끝 셀 → (newX, newY, newSpanX, newSpanY) 계산 후 clamp.
     *
     * @param dragEndCellXLocal BottomStart 전용: absGridX 기반 X 셀 (그 외는 dragEndCellX 사용)
     */
    private data class PositionAndSpan(val newX: Int, val newY: Int, val newSpanX: Int, val newSpanY: Int)

    private fun computePositionAndSpanForCorner(
        resizeCorner: ResizeCorner,
        currentItem: GridItem,
        dragEndCellX: Int,
        dragEndCellY: Int,
        dragEndCellXLocal: Int,
        fixedRight: Int,
        fixedBottom: Int,
        gridSize: GridSize
    ): PositionAndSpan = when (resizeCorner) {
        ResizeCorner.BottomEnd -> throw IllegalArgumentException("BottomEnd does not use computePositionAndSpanForCorner")
        ResizeCorner.TopStart -> {
            var newX = dragEndCellX.coerceIn(0, fixedRight)
            var newY = dragEndCellY.coerceIn(0, fixedBottom)
            var newSpanX = (fixedRight - newX + 1).coerceAtLeast(1)
            var newSpanY = (fixedBottom - newY + 1).coerceAtLeast(1)
            val clampResult = ResizeSpanCalculator.clampResizeResult(newX, newY, newSpanX, newSpanY, gridSize)
            val (clampedX, clampedY) = clampResult.first
            val (clampedSpanX, clampedSpanY) = clampResult.second
            PositionAndSpan(clampedX, clampedY, clampedSpanX, clampedSpanY)
        }
        ResizeCorner.TopEnd -> {
            val dragEndX = dragEndCellX.coerceIn(currentItem.x, gridSize.columns - 1)
            var newY = dragEndCellY.coerceIn(0, fixedBottom)
            var newSpanX = (dragEndX - currentItem.x + 1).coerceAtLeast(1)
            var newSpanY = (fixedBottom - newY + 1).coerceAtLeast(1)
            val clampResult = ResizeSpanCalculator.clampResizeResult(currentItem.x, newY, newSpanX, newSpanY, gridSize)
            val (_, clampedY) = clampResult.first
            val (clampedSpanX, clampedSpanY) = clampResult.second
            PositionAndSpan(currentItem.x, clampedY, clampedSpanX, clampedSpanY)
        }
        ResizeCorner.BottomStart -> {
            var newX = dragEndCellXLocal.coerceIn(0, fixedRight)
            val dragEndY = dragEndCellY.coerceIn(currentItem.y, gridSize.rows - 1)
            var newSpanX = (fixedRight - newX + 1).coerceAtLeast(1)
            var newSpanY = (dragEndY - currentItem.y + 1).coerceAtLeast(1)
            val clampResult = ResizeSpanCalculator.clampResizeResult(newX, currentItem.y, newSpanX, newSpanY, gridSize)
            val (clampedX, _) = clampResult.first
            val (clampedSpanX, clampedSpanY) = clampResult.second
            PositionAndSpan(clampedX, currentItem.y, clampedSpanX, clampedSpanY)
        }
    }

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
                    val (_, previewSizePx) = computePreviewOffsetAndSize(
                        resizeCorner, gridPos, itemOffsetPx, currentItem,
                        0f, 0f, 0f, cellWidthPx, cellHeightPx, gridSize
                    )
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
                    val snap = snapshotFixedEdges(
                        resizeCorner, currentItem, cellWidthPx,
                        dragStartFixedRight, dragStartFixedBottom, dragStartItemOffsetXPx
                    )
                    if (snap.newDragStartFixedRight >= 0) dragStartFixedRight = snap.newDragStartFixedRight
                    if (snap.newDragStartFixedBottom >= 0) dragStartFixedBottom = snap.newDragStartFixedBottom
                    val fixedRight = snap.fixedRight
                    val fixedBottom = snap.fixedBottom
                    val fixedRightPx = (fixedRight + 1) * cellWidthPx
                    val fixedBottomPx = (fixedBottom + 1) * cellHeightPx
                    val (previewOffsetPx, previewSizePx) = computePreviewOffsetAndSize(
                        resizeCorner, gridPos, itemOffsetPx, currentItem,
                        fixedRightPx, fixedBottomPx, 0f, cellWidthPx, cellHeightPx, gridSize
                    )
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    val pos = computePositionAndSpanForCorner(
                        resizeCorner, currentItem, dragEndCellX, dragEndCellY, dragEndCellX,
                        fixedRight, fixedBottom, gridSize
                    )
                    val newX = pos.newX
                    val newY = pos.newY
                    val newSpanX = pos.newSpanX
                    val newSpanY = pos.newSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newX == currentItem.x && newY == currentItem.y && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val successOffsetPx = newX * cellWidthPx to newY * cellHeightPx
                    val successSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                    if (!applyMoveAndResize(
                            item.id, newX, newY, targetSpanX, targetSpanY,
                            items, gridSize, bridge, successOffsetPx, successSizePx
                        ) { o, s -> lastPreviewOffsetPx = o; lastPreviewSizePx = s }
                    ) return@detectDragGestures
                }
                ResizeCorner.TopEnd -> {
                    val snap = snapshotFixedEdges(
                        resizeCorner, currentItem, cellWidthPx,
                        dragStartFixedRight, dragStartFixedBottom, dragStartItemOffsetXPx
                    )
                    if (snap.newDragStartFixedBottom >= 0) dragStartFixedBottom = snap.newDragStartFixedBottom
                    val fixedBottom = snap.fixedBottom
                    val fixedBottomPx = (fixedBottom + 1) * cellHeightPx
                    val (previewOffsetPx, previewSizePx) = computePreviewOffsetAndSize(
                        resizeCorner, gridPos, itemOffsetPx, currentItem,
                        0f, fixedBottomPx, 0f, cellWidthPx, cellHeightPx, gridSize
                    )
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    val pos = computePositionAndSpanForCorner(
                        resizeCorner, currentItem, dragEndCellX, dragEndCellY, dragEndCellX,
                        snap.fixedRight, fixedBottom, gridSize
                    )
                    val newY = pos.newY
                    val newSpanX = pos.newSpanX
                    val newSpanY = pos.newSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newY == currentItem.y && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val successOffsetPx = currentItem.x * cellWidthPx to newY * cellHeightPx
                    val successSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                    if (!applyMoveAndResize(
                            item.id, currentItem.x, newY, targetSpanX, targetSpanY,
                            items, gridSize, bridge, successOffsetPx, successSizePx
                        ) { o, s -> lastPreviewOffsetPx = o; lastPreviewSizePx = s }
                    ) return@detectDragGestures
                }
                ResizeCorner.BottomStart -> {
                    val snap = snapshotFixedEdges(
                        resizeCorner, currentItem, cellWidthPx,
                        dragStartFixedRight, dragStartFixedBottom, dragStartItemOffsetXPx
                    )
                    if (snap.newDragStartFixedRight >= 0) dragStartFixedRight = snap.newDragStartFixedRight
                    if (snap.newDragStartItemOffsetXPx >= 0f) dragStartItemOffsetXPx = snap.newDragStartItemOffsetXPx
                    val fixedRight = snap.fixedRight
                    val fixedRightPx = (fixedRight + 1) * cellWidthPx
                    val absGridX = change.position.x + snap.dragStartItemOffsetXPx
                    val dragEndCellXLocal = ((absGridX - 0.5f * cellWidthPx) / cellWidthPx).toInt()
                        .coerceIn(0, gridSize.columns - 1)
                    val (previewOffsetPx, previewSizePx) = computePreviewOffsetAndSize(
                        resizeCorner, gridPos, itemOffsetPx, currentItem,
                        fixedRightPx, 0f, absGridX, cellWidthPx, cellHeightPx, gridSize
                    )
                    onPreviewOffsetPxChange(previewOffsetPx)
                    onPreviewSizePxChange(previewSizePx)
                    lastPreviewOffsetPx = previewOffsetPx
                    lastPreviewSizePx = previewSizePx
                    val pos = computePositionAndSpanForCorner(
                        resizeCorner, currentItem, dragEndCellX, dragEndCellY, dragEndCellXLocal,
                        fixedRight, snap.fixedBottom, gridSize
                    )
                    val newX = pos.newX
                    val newSpanX = pos.newSpanX
                    val newSpanY = pos.newSpanY
                    onPreviewSpanChange(newSpanX to newSpanY)
                    lastDisplaySpanX = newSpanX
                    lastDisplaySpanY = newSpanY
                    val (targetSpanX, targetSpanY) = ResizeSpanCalculator.computeSpanWithHysteresis(
                        currentItem, newSpanX, newSpanY, lastSpanX, lastSpanY, gridSize, hysteresisCells = 1
                    )
                    lastSpanX = targetSpanX
                    lastSpanY = targetSpanY
                    if (newX == currentItem.x && targetSpanX == currentItem.spanX && targetSpanY == currentItem.spanY) return@detectDragGestures
                    val successOffsetPx = newX * cellWidthPx to currentItem.y * cellHeightPx
                    val successSizePx = targetSpanX * cellWidthPx to targetSpanY * cellHeightPx
                    if (!applyMoveAndResize(
                            item.id, newX, currentItem.y, targetSpanX, targetSpanY,
                            items, gridSize, bridge, successOffsetPx, successSizePx
                        ) { o, s -> lastPreviewOffsetPx = o; lastPreviewSizePx = s }
                    ) return@detectDragGestures
                }
            }
        }
    }
}
