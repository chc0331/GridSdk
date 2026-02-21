package com.android.gridsdk.library.internal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.android.gridsdk.library.internal.ui.DragGestureHandler.dragGesture
import com.android.gridsdk.library.internal.ui.ResizeGestureHandler.resizeGesture
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import kotlin.math.roundToInt

/**
 * 내부 그리드 레이아웃 구현
 *
 * - Box 기반 절대 위치 배치
 * - 셀 크기 계산 (fillMaxSize 기반)
 * - 드래그/리사이즈 제스처 조합 (resize 우선, move 차순)
 * - 위치/크기 애니메이션
 */
@InternalApi
internal object GridLayoutInternal {

    @Composable
    internal fun GridLayoutContent(
        gridSize: GridSize,
        items: List<GridItem>,
        onItemsChange: (List<GridItem>) -> Unit,
        cellWidthPx: Float,
        cellHeightPx: Float,
        onFailure: ((com.android.gridsdk.library.model.GridError) -> Unit)?,
        cellContent: @Composable (GridItem) -> Unit
    ) {
        val bridge = remember {
            EngineStateBridge(onItemsChange, onFailure)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            items.forEach { item ->
                key(item.id) {
                    GridItemCell(
                        item = item,
                        items = items,
                        gridSize = gridSize,
                        cellWidthPx = cellWidthPx,
                        cellHeightPx = cellHeightPx,
                        bridge = bridge,
                        cellContent = cellContent
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
        cellWidthPx: Float,
        cellHeightPx: Float,
        bridge: EngineStateBridge,
        cellContent: @Composable (GridItem) -> Unit
    ) {
        val offsetXPx = item.x * cellWidthPx
        val offsetYPx = item.y * cellHeightPx
        val widthPx = item.spanX * cellWidthPx
        val heightPx = item.spanY * cellHeightPx
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
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
                .size(widthDp, heightDp)
                .resizeGesture(
                    item = item,
                    items = items,
                    gridSize = gridSize,
                    cellWidthPx = cellWidthPx,
                    cellHeightPx = cellHeightPx,
                    bridge = bridge,
                    previousSpanX = item.spanX,
                    previousSpanY = item.spanY
                )
                .dragGesture(
                    item = item,
                    items = items,
                    gridSize = gridSize,
                    cellWidthPx = cellWidthPx,
                    cellHeightPx = cellHeightPx,
                    bridge = bridge
                )
                .then(
                    Modifier.minimumInteractiveComponentSize()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Grid item at (${item.x}, ${item.y}), size ${item.spanX}x${item.spanY}"
                        }
                ),
            content = { cellContent(item) }
        )
    }
}
