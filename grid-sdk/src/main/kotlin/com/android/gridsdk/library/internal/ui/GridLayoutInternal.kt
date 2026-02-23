package com.android.gridsdk.library.internal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.State
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import com.android.gridsdk.library.internal.ui.DragGestureHandler.dragGesture
import com.android.gridsdk.library.internal.ui.ResizeGestureHandler.resizeHandleGesture
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.internal.util.ResizeCorner
import com.android.gridsdk.library.internal.util.toPx
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import kotlin.math.roundToInt

internal val ResizeOutlineStrokeWidthDp = 3.dp
internal val CornerHandleSizeDp = 28.dp
internal val CornerHandleVisualRadiusDp = 6.dp

/**
 * 내부 그리드 레이아웃 구현
 *
 * - Box 기반 절대 위치 배치
 * - 셀 크기 계산 (fillMaxSize 기반)
 * - 드래그: 아이템 이동
 * - 롱프레스: 리사이즈 핸들러 표시 → 핸들러 드래그로 리사이즈
 * - 위치/크기 애니메이션
 */
@InternalApi
@Composable
internal fun GridLayoutContent(
    gridSize: GridSize,
    items: List<GridItem>,
    onItemsChange: (List<GridItem>) -> Unit,
    cellWidth: Dp,
    cellHeight: Dp,
    onFailure: ((com.android.gridsdk.library.model.GridError) -> Unit)?,
    cellContent: @Composable (GridItem) -> Unit
) {
    val bridge = remember {
        EngineStateBridge(onItemsChange, onFailure)
    }
    var resizeModeItemId by remember { mutableStateOf<String?>(null) }
    var resizePreviewSpan by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var resizePreviewSizePx by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var resizePreviewOffsetPx by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    val density = LocalDensity.current
    val cornerHandleSizePx = with(density) { CornerHandleSizeDp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (resizeModeItemId != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        resizeModeItemId = null
                        resizePreviewSpan = null
                        resizePreviewSizePx = null
                        resizePreviewOffsetPx = null
                    }
            )
        }
        items.forEach { item ->
            key(item.id) {
                GridItemCell(
                    item = item,
                    items = items,
                    gridSize = gridSize,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    bridge = bridge,
                    cellContent = cellContent,
                    resizeModeItemId = resizeModeItemId,
                    onShowResizeHandle = {
                        resizeModeItemId = it
                        resizePreviewSpan = null
                    },
                    onClearResizeMode = {
                        resizeModeItemId = null
                        resizePreviewSpan = null
                        resizePreviewSizePx = null
                        resizePreviewOffsetPx = null
                    }
                )
            }
        }
        resizeModeItemId?.let { itemId ->
            items.find { it.id == itemId }?.let { item ->
                val displaySpanX = resizePreviewSpan?.first ?: item.spanX
                val displaySpanY = resizePreviewSpan?.second ?: item.spanY
                ResizeOutlineOverlay(
                    item = item,
                    displaySpanX = displaySpanX,
                    displaySpanY = displaySpanY,
                    resizePreviewSizePx = resizePreviewSizePx,
                    resizePreviewOffsetPx = resizePreviewOffsetPx,
                    items = items,
                    gridSize = gridSize,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    cornerHandleSizePx = cornerHandleSizePx,
                    bridge = bridge,
                    onPreviewSpanChange = { span ->
                        resizePreviewSpan = span
                    },
                    onPreviewSizePxChange = { sizePx ->
                        resizePreviewSizePx = sizePx
                    },
                    onPreviewOffsetPxChange = { offsetPx ->
                        resizePreviewOffsetPx = offsetPx
                    },
                    onClearResizeMode = {
                        resizeModeItemId = null
                        resizePreviewSpan = null
                        resizePreviewSizePx = null
                        resizePreviewOffsetPx = null
                    }
                )
            }
        }
    }
}

@Composable
private fun GridItemCell(
    item: GridItem,
    items: List<GridItem>,
    gridSize: GridSize,
    cellWidth: Dp,
    cellHeight: Dp,
    bridge: EngineStateBridge,
    cellContent: @Composable (GridItem) -> Unit,
    resizeModeItemId: String?,
    onShowResizeHandle: (String) -> Unit,
    onClearResizeMode: () -> Unit
) {
    val offsetXPx = item.x * (cellWidth.toPx())
    val offsetYPx = item.y * cellHeight.toPx()
    val (widthPx, heightPx) = (item.spanX * cellWidth.toPx()) to (item.spanY * cellHeight.toPx())
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetXPx,
        animationSpec = tween(durationMillis = 200),
        label = "gridItemOffsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetYPx,
        animationSpec = tween(durationMillis = 200),
        label = "gridItemOffsetY"
    )
    val animatedWidth by animateFloatAsState(
        targetValue = widthPx,
        animationSpec = tween(durationMillis = 200),
        label = "gridItemWidth"
    )
    val animatedHeight by animateFloatAsState(
        targetValue = heightPx,
        animationSpec = tween(durationMillis = 200),
        label = "gridItemHeight"
    )
    val density = LocalDensity.current
    val widthDp = with(density) { animatedWidth.toDp() }
    val heightDp = with(density) { animatedHeight.toDp() }
    val isResizeMode = resizeModeItemId == item.id

    Box(
        modifier = Modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .size(widthDp, heightDp)
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onShowResizeHandle(item.id) },
                    onTap = { if (isResizeMode) onClearResizeMode() }
                )
            }
            .dragGesture(
                item = item,
                items = items,
                gridSize = gridSize,
                cellWidthPx = cellWidth.toPx(),
                cellHeightPx = cellHeight.toPx(),
                bridge = bridge
            )
            .then(
                Modifier.minimumInteractiveComponentSize()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Grid item at (${item.x}, ${item.y}), size ${item.spanX}x${item.spanY}"
                        testTag = "grid-item-${item.id}"
                    }
            ),
        content = {
            cellContent(item)
            if (isResizeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onClearResizeMode()
                        }
                )
            }
        }
    )
}
