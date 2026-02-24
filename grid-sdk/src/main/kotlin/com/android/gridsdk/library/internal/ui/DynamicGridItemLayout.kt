package com.android.gridsdk.library.internal.ui

import androidx.compose.animation.core.animateSizeAsState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.gridItemData
import com.android.gridsdk.library.internal.resize.BottomEndStrategy
import com.android.gridsdk.library.internal.resize.ResizeStrategy
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
    onLongPressed: (String) -> Unit,
    onTap: (String) -> Unit,
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