package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.internal.resize.BottomEndStrategy
import com.android.gridsdk.library.internal.ui.ResizeGestureHandler.resizeHandleGesture
import com.android.gridsdk.library.internal.util.ResizeCorner
import com.android.gridsdk.library.internal.util.toPx
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import kotlin.math.roundToInt

@Composable
internal fun ResizeOutlineOverlay(
    item: GridItem,
    displaySpanX: Int,
    displaySpanY: Int,
    resizePreviewSizePx: Pair<Float, Float>?,
    resizePreviewOffsetPx: Pair<Float, Float>?,
    items: List<GridItem>,
    gridSize: GridSize,
    cellWidth: Dp,
    cellHeight: Dp,
    cornerHandleSizePx: Float,
    bridge: EngineStateBridge,
    onPreviewSpanChange: (Pair<Int, Int>?) -> Unit,
    onPreviewSizePxChange: (Pair<Float, Float>?) -> Unit,
    onPreviewOffsetPxChange: (Pair<Float, Float>?) -> Unit,
    onClearResizeMode: () -> Unit
) {
    // 1. declare strategy
    val strategy = remember {
        BottomEndStrategy(
            initialSize = DpSize(
                (cellWidth.value * item.spanX).dp,
                (cellHeight.value * item.spanY).dp
            ), initialSpanX = item.spanX,
            initialSpanY = item.spanY, cellWidth = cellWidth, cellHeight = cellHeight
        )
    }
    val offsetXPx = resizePreviewOffsetPx?.first ?: (item.x * cellWidth.toPx())
    val offsetYPx = resizePreviewOffsetPx?.second ?: (item.y * cellHeight.toPx())

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .size(strategy.rawSize)
    ) {
        ResizeHandler(
            type = ResizeCorner.BottomEnd,
            size = strategy.rawSize,
            onResizeStart = { offset ->
                strategy.onResizeStart(offset)
            },
            onResize = { deltaW, deltaH, dragAmount ->
                strategy.onResize(
                    item,
                    deltaW,
                    deltaH,
                    dragAmount
                ) { spanX, spanY, offsetX, offsetY ->

                }
                true
            },
            onResizeEnd = {
                strategy.onResizeEnd { spanX, spanY ->

                }
            }
        )
    }
}

@Composable
private fun ResizeCornerHandle(
    modifier: Modifier,
    resizeCorner: ResizeCorner,
    cornerHandleSizePx: Float,
    currentOverlayWidthPx: Float,
    currentOverlayHeightPx: Float,
    item: GridItem,
    items: List<GridItem>,
    itemsState: State<List<GridItem>>,
    gridSize: GridSize,
    cellWidthPx: Float,
    cellHeightPx: Float,
    bridge: EngineStateBridge,
    itemId: String,
    onPreviewSpanChange: (Pair<Int, Int>?) -> Unit,
    onPreviewSizePxChange: (Pair<Float, Float>?) -> Unit,
    onPreviewOffsetPxChange: (Pair<Float, Float>?) -> Unit = {}
) {
    val overlaySizeState = rememberUpdatedState(currentOverlayWidthPx to currentOverlayHeightPx)
    val baseModifier = modifier
        .size(CornerHandleSizeDp)
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) { }
        .resizeHandleGesture(
            item = item,
            itemsState = itemsState,
            gridSize = gridSize,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            cornerHandleSizePx = cornerHandleSizePx,
            overlaySizeState = overlaySizeState,
            resizeCorner = resizeCorner,
            bridge = bridge,
            previousSpanX = item.spanX,
            previousSpanY = item.spanY,
            onPreviewSpanChange = onPreviewSpanChange,
            onPreviewSizePxChange = onPreviewSizePxChange,
            onPreviewOffsetPxChange = onPreviewOffsetPxChange
        )
        .semantics {
            contentDescription = "Resize handle"
            testTag = "resize-handle-$itemId"
        }
    val outlineColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(CornerHandleVisualRadiusDp * 2)) {
            drawCircle(
                color = outlineColor,
                radius = size.minDimension / 2
            )
        }
    }
}