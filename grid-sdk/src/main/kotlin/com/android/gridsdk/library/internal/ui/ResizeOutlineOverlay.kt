package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.internal.ui.ResizeGestureHandler.resizeHandleGesture
import com.android.gridsdk.library.internal.util.ResizeCorner
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
    cellWidthPx: Float,
    cellHeightPx: Float,
    cornerHandleSizePx: Float,
    bridge: EngineStateBridge,
    onPreviewSpanChange: (Pair<Int, Int>?) -> Unit,
    onPreviewSizePxChange: (Pair<Float, Float>?) -> Unit,
    onPreviewOffsetPxChange: (Pair<Float, Float>?) -> Unit,
    onClearResizeMode: () -> Unit
) {
    val updatedOnPreviewSpanChange = rememberUpdatedState(onPreviewSpanChange)
    val updatedOnPreviewSizePxChange = rememberUpdatedState(onPreviewSizePxChange)
    val updatedOnPreviewOffsetPxChange = rememberUpdatedState(onPreviewOffsetPxChange)
    val itemsState = rememberUpdatedState(items)
    val offsetXPx = resizePreviewOffsetPx?.first ?: (item.x * cellWidthPx)
    val offsetYPx = resizePreviewOffsetPx?.second ?: (item.y * cellHeightPx)
    val (widthPx, heightPx) = when (val sizePx = resizePreviewSizePx) {
        null -> displaySpanX * cellWidthPx to displaySpanY * cellHeightPx
        else -> sizePx.first to sizePx.second
    }
    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = with(density) { heightPx.toDp() }
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .size(widthDp, heightDp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .border(
                    width = ResizeOutlineStrokeWidthDp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    onClearResizeMode()
                }
        )
        ResizeCornerHandle(
            modifier = Modifier.align(Alignment.TopStart),
            resizeCorner = ResizeCorner.TopStart,
            cornerHandleSizePx = cornerHandleSizePx,
            currentOverlayWidthPx = widthPx,
            currentOverlayHeightPx = heightPx,
            item = item,
            items = items,
            itemsState = itemsState,
            gridSize = gridSize,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            bridge = bridge,
            itemId = item.id,
            onPreviewSpanChange = { span -> updatedOnPreviewSpanChange.value(span) },
            onPreviewSizePxChange = { sizePx -> updatedOnPreviewSizePxChange.value(sizePx) },
            onPreviewOffsetPxChange = { offsetPx -> updatedOnPreviewOffsetPxChange.value(offsetPx) }
        )
        ResizeCornerHandle(
            modifier = Modifier.align(Alignment.TopEnd),
            resizeCorner = ResizeCorner.TopEnd,
            cornerHandleSizePx = cornerHandleSizePx,
            currentOverlayWidthPx = widthPx,
            currentOverlayHeightPx = heightPx,
            item = item,
            items = items,
            itemsState = itemsState,
            gridSize = gridSize,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            bridge = bridge,
            itemId = item.id,
            onPreviewSpanChange = { span -> updatedOnPreviewSpanChange.value(span) },
            onPreviewSizePxChange = { sizePx -> updatedOnPreviewSizePxChange.value(sizePx) },
            onPreviewOffsetPxChange = { offsetPx -> updatedOnPreviewOffsetPxChange.value(offsetPx) }
        )
        ResizeCornerHandle(
            modifier = Modifier.align(Alignment.BottomStart),
            resizeCorner = ResizeCorner.BottomStart,
            cornerHandleSizePx = cornerHandleSizePx,
            currentOverlayWidthPx = widthPx,
            currentOverlayHeightPx = heightPx,
            item = item,
            items = items,
            itemsState = itemsState,
            gridSize = gridSize,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            bridge = bridge,
            itemId = item.id,
            onPreviewSpanChange = { span -> updatedOnPreviewSpanChange.value(span) },
            onPreviewSizePxChange = { sizePx -> updatedOnPreviewSizePxChange.value(sizePx) },
            onPreviewOffsetPxChange = { offsetPx -> updatedOnPreviewOffsetPxChange.value(offsetPx) }
        )
        ResizeCornerHandle(
            modifier = Modifier.align(Alignment.BottomEnd),
            resizeCorner = ResizeCorner.BottomEnd,
            cornerHandleSizePx = cornerHandleSizePx,
            currentOverlayWidthPx = widthPx,
            currentOverlayHeightPx = heightPx,
            item = item,
            items = items,
            itemsState = itemsState,
            gridSize = gridSize,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            bridge = bridge,
            itemId = item.id,
            onPreviewSpanChange = { span -> updatedOnPreviewSpanChange.value(span) },
            onPreviewSizePxChange = { sizePx -> updatedOnPreviewSizePxChange.value(sizePx) },
            onPreviewOffsetPxChange = { offsetPx -> updatedOnPreviewOffsetPxChange.value(offsetPx) }
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