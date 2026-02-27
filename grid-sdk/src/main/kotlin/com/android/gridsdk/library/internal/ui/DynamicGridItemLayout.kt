package com.android.gridsdk.library.internal.ui

import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.gridItemData
import com.android.gridsdk.library.internal.resize.BottomEndStrategy
import com.android.gridsdk.library.internal.resize.TopEndStrategy
import com.android.gridsdk.library.internal.util.ResizeCorner
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

@Composable
internal fun DynamicGridItemLayout(
    gridSize: GridSize,
    item: GridItem,
    cellWidth: Dp,
    cellHeight: Dp,
    isResizeMode: Boolean,
    showHandlersTrigger: Boolean,
    onLongPressed: (String) -> Unit,
    onTap: (String) -> Unit,
    onHandlersHidden: () -> Unit,
    onItemChanged: (GridItem) -> Unit,
    modifier: Modifier = Modifier,
    cellContent: @Composable (GridItem) -> Unit
) {
    val cellSize = LocalGridCellSize.current
    var itemWidth by remember { mutableStateOf(cellSize.width * item.spanX) }
    var itemHeight by remember { mutableStateOf(cellSize.height * item.spanY) }
    // 상위 레이아웃이 Modifier.wrapContentSize() 일경우 Flickering 이슈 발생.
    val itemSize by animateSizeAsState(
        targetValue = Size(itemWidth.value, itemHeight.value),
        animationSpec = tween(200),
        label = "gridItemSize"
    )

    var currentItem by remember { mutableStateOf(item) }

    Box(
        modifier = modifier
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPressed(item.id) },
                    onTap = { onTap(item.id) }
                )
            }
            .gridItemData(currentItem)
    ) {
        Box(
            modifier = Modifier.size(DpSize(itemSize.width.dp, itemSize.height.dp))
        ) {
            cellContent(currentItem)
        }
        if (isResizeMode) {
            DynamicResizeItemLayout(
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                itemWidth = itemWidth,
                itemHeight = itemHeight,
                item = item,
                showHandlersTrigger = showHandlersTrigger,
                onHandlersHidden = onHandlersHidden,
                onUpdateItem = { width, height, updateItem ->
                    itemWidth = width
                    itemHeight = height
                    currentItem = updateItem
                    onItemChanged(updateItem)
                }
            )
        }
    }
}

private sealed class HandlerVisibilityState {
    object AllVisible : HandlerVisibilityState()
    data class Dragging(val corner: ResizeCorner) : HandlerVisibilityState()
    object AllHidden : HandlerVisibilityState()
}

@Composable
private fun DynamicResizeItemLayout(
    cellWidth: Dp,
    cellHeight: Dp,
    itemWidth: Dp,
    itemHeight: Dp,
    item: GridItem,
    showHandlersTrigger: Boolean,
    onHandlersHidden: () -> Unit,
    onUpdateItem: (Dp, Dp, GridItem) -> Unit
) {
    var handlerVisibilityState by remember(showHandlersTrigger) {
        mutableStateOf(
            if (showHandlersTrigger) HandlerVisibilityState.AllVisible
            else HandlerVisibilityState.AllHidden
        )
    }
    var handlerSize by remember {
        mutableStateOf(DpSize(itemWidth, itemHeight))
    }

    val topEndStrategy = remember {
        TopEndStrategy(
            initialSize = DpSize(itemWidth, itemHeight),
            initialSpanX = item.spanX,
            initialSpanY = item.spanY,
            cellWidth = cellWidth,
            cellHeight = cellHeight
        )
    }
    val bottomEndStrategy = remember {
        BottomEndStrategy(
            initialSize = DpSize(itemWidth, itemHeight),
            initialSpanX = item.spanX,
            initialSpanY = item.spanY,
            cellWidth = cellWidth,
            cellHeight = cellHeight
        )
    }

    val showTopEnd = when (val s = handlerVisibilityState) {
        is HandlerVisibilityState.AllVisible -> true
        is HandlerVisibilityState.Dragging -> s.corner == ResizeCorner.TopEnd
        is HandlerVisibilityState.AllHidden -> false
    }
    val showBottomEnd = when (val s = handlerVisibilityState) {
        is HandlerVisibilityState.AllVisible -> true
        is HandlerVisibilityState.Dragging -> s.corner == ResizeCorner.BottomEnd
        is HandlerVisibilityState.AllHidden -> false
    }

    if (handlerVisibilityState != HandlerVisibilityState.AllHidden) {
        Spacer(
            modifier = Modifier
                .size(handlerSize)
                .background(Color.Transparent, RoundedCornerShape(18.dp))
                .border(2.dp, Color.White, RoundedCornerShape(18.dp))
        )
    }

    if (showTopEnd) {
        ResizeHandler(
            item = item,
            type = ResizeCorner.TopEnd,
            strategy = topEndStrategy,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            onResizeStart = {
                handlerVisibilityState = HandlerVisibilityState.Dragging(ResizeCorner.TopEnd)
            },
            onResize = {
                handlerSize = it
            },
            onResizeEnd = {
                handlerVisibilityState = HandlerVisibilityState.AllHidden
                onHandlersHidden()
                bottomEndStrategy.rawSize = topEndStrategy.rawSize
                handlerSize = bottomEndStrategy.rawSize
            },
            onUpdateItem = onUpdateItem
        )
    }

    if (showBottomEnd) {
        ResizeHandler(
            item = item,
            type = ResizeCorner.BottomEnd,
            strategy = bottomEndStrategy,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            onResizeStart = {
                handlerVisibilityState = HandlerVisibilityState.Dragging(ResizeCorner.BottomEnd)
            },
            onResize = {
                handlerSize = it
            },
            onResizeEnd = {
                handlerVisibilityState = HandlerVisibilityState.AllHidden
                onHandlersHidden()
                topEndStrategy.rawSize = bottomEndStrategy.rawSize
                handlerSize = topEndStrategy.rawSize
            },
            onUpdateItem = onUpdateItem
        )
    }
}