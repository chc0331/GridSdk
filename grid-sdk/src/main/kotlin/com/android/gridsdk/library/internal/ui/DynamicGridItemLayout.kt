package com.android.gridsdk.library.internal.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.android.gridsdk.library.internal.resize.BottomEndStrategy
import com.android.gridsdk.library.internal.resize.ResizeStrategy
import com.android.gridsdk.library.internal.util.ResizeCorner
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

@Composable
internal fun DynamicGridItemLayout(
    modifier: Modifier = Modifier,
    gridSize: GridSize,
    item: GridItem,
    cellWidth: Dp,
    cellHeight: Dp,
    isResizeMode: Boolean,
    onLongPressed: (String) -> Unit,
    onTap: (String) -> Unit,
    cellContent: @Composable (GridItem) -> Unit
) {
    val density = LocalDensity.current
    val cellSize = LocalGridCellSize.current
    var itemWidth by remember { mutableStateOf(cellSize.width * item.spanX) }
    var itemHeight by remember { mutableStateOf(cellSize.height * item.spanY) }

    val widthPx = with(density) { itemWidth.toPx() }
    val heightPx = with(density) { itemHeight.toPx() }
    val animatedWidth by animateFloatAsState(
        targetValue = widthPx,
        animationSpec = tween(200),
        label = "gridItemWidth"
    )
    val animatedHeight by animateFloatAsState(
        targetValue = heightPx,
        animationSpec = tween(200),
        label = "gridItemHeight"
    )
    val widthDp = with(density) { animatedWidth.toDp() }
    val heightDp = with(density) { animatedHeight.toDp() }

    var currentItem by remember { mutableStateOf(item) }

    Box(
        modifier = modifier
            .size(widthDp, heightDp)
            .pointerInput(item.id) {
                detectTapGestures(
                    onLongPress = { onLongPressed(item.id) },
                    onTap = { onTap(item.id) }
                )
            }
    ) {
        cellContent(currentItem)
        if (isResizeMode) {
            DynamicResizeItemLayout(
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                itemWidth = itemWidth,
                itemHeight = itemHeight,
                item = item,
                onUpdateItem = { width, height, updateItem ->
                    itemWidth = width
                    itemHeight = height
                    Log.i("heec.choi", "Before : $currentItem")
                    Log.i("heec.choi", "After : $updateItem")
                    currentItem = updateItem
                }
            )
        }
    }
}

@Composable
private fun DynamicResizeItemLayout(
    cellWidth: Dp,
    cellHeight: Dp,
    itemWidth: Dp,
    itemHeight: Dp,
    item: GridItem,
    onUpdateItem: (Dp, Dp, GridItem) -> Unit
) {
    // 1. declare strategy
    val strategy = remember {
        BottomEndStrategy(
            initialSize = DpSize(itemWidth, itemHeight),
            initialSpanX = item.spanX,
            initialSpanY = item.spanY,
            cellWidth = cellWidth,
            cellHeight = cellHeight
        )
    }

    Box(
        modifier = Modifier
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
                    val nextWidth = cellWidth * spanX
                    val nextHeight = cellHeight * spanY
                    onUpdateItem(
                        nextWidth, nextHeight, item.copy(
                            spanX = spanX, spanY = spanY,
                            x = offsetX, y = offsetY
                        )
                    )
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
private fun BottomEndHandler(
    item: GridItem,
    strategy: ResizeStrategy,
    cellWidth: Dp, cellHeight: Dp,
    onUpdateItem: (Dp, Dp) -> Unit
) {
    Box(
        modifier = Modifier
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
                    val nextWidth = cellWidth * spanX
                    val nextHeight = cellHeight * spanY
                    onUpdateItem(nextWidth, nextHeight)
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