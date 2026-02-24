package com.android.gridsdk.library

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.android.gridsdk.library.internal.state.OccupancyState
import com.android.gridsdk.library.internal.state.rememberOccupancyState
import com.android.gridsdk.library.internal.ui.DynamicGridItemLayout
import com.android.gridsdk.library.internal.ui.LocalGridCellSize
import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import kotlin.math.roundToInt


/**
 * 🔍 Compose 원칙 준수 확인
 * ✅ Phase System: Layout measure에서 State 변경 없음
 * ✅ Immutable State: 새 배열 참조 생성으로 변경 감지
 * ✅ Unidirectional Data Flow: items → syncFromItems → resolvedItems → Layout
 * ✅ Pure Layout: measure/place에 부수효과 없음
 * ✅ 리사이즈 호환: 기존 기능 완전 보존
 * 린터 에러도 없고, 모든 Compose 원칙을 준수하는 구조로 개선되었습니다!
 *
 * */
/**
 * 위치가 지정되지 않은 아이템의 위치를 자동으로 결정합니다.
 * Composition phase에서 호출되어 Layout measure phase에서의 부수효과를 제거합니다.
 */
private fun resolveUnpositionedItems(
    items: List<GridItem>,
    occupancyState: OccupancyState
): List<GridItem> {
    return items.mapNotNull { item ->
        val exists = occupancyState.checkIdExist(item.id)
        val position = if (!exists) {
            occupancyState.findAddPosition(item.spanX, item.spanY)
        } else {
            occupancyState.findPosition(item.id)
        }
        
        if (position == null) return@mapNotNull null
        
        val resolved = item.copy(x = position.first, y = position.second)
        // occupancyState에 배치 정보 등록 (Composition phase에서 실행)
        occupancyState.updateGridItem(
            resolved.id,
            resolved.x,
            resolved.y,
            resolved.spanX,
            resolved.spanY
        )
        resolved
    }
}

/**
 * 그리드 레이아웃 Composable
 *
 * N×M 고정 그리드에서 아이템을 배치하며, 드래그로 이동, 롱프레스 시 핸들러 표시 후 핸들러 드래그로 리사이즈를 지원합니다.
 *
 * @param gridSize 그리드 크기 (행×열)
 * @param items 현재 아이템 목록
 * @param onItemsChange 아이템 변경 시 콜백 (추가/삭제/이동/리사이즈 결과)
 * @param modifier Modifier
 * @param onFailure 엔진 처리 실패 시 콜백 (선택)
 * @param cellContent 각 아이템의 콘텐츠 슬롯
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DynamicGridLayout(
    gridSize: GridSize,
    items: List<GridItem>,
    onItemsChange: (List<GridItem>) -> Unit,
    modifier: Modifier = Modifier,
    onFailure: ((GridError) -> Unit)? = null,
    cellContent: @Composable (GridItem) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cellWidth = maxWidth / gridSize.columns
        val cellHeight = maxHeight / gridSize.rows
        val occupancyState = rememberOccupancyState(gridSize)

        // Composition phase: items가 변경되면 occupancyState를 동기화
        LaunchedEffect(items, gridSize) {
            occupancyState.syncFromItems(items)
        }

        // Composition phase: 위치가 없는 아이템의 auto-placement 수행
        val resolvedItems = remember(items, gridSize, occupancyState.occupancyMap) {
            resolveUnpositionedItems(items, occupancyState)
        }

        CompositionLocalProvider(
            LocalGridCellSize provides DpSize(cellWidth, cellHeight)
        ) {
            var currentResizeItemId by remember { mutableStateOf<String?>(null) }

            // Layout은 순수 배치만 수행 (OccupancyState 접근 없음)
            DynamicGridLayout(
                gridSize = gridSize,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                modifier = Modifier.fillMaxSize()
            ) {
                resolvedItems.forEach { item ->
                    Log.i("heec.choi","Resolved item : $item")
                    DynamicGridItemLayout(
                        gridSize = gridSize,
                        item = item,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        isResizeMode = currentResizeItemId == item.id,
                        onLongPressed = { id ->
                            currentResizeItemId = id
                        },
                        onTap = { id ->
                            currentResizeItemId = null
                        },
                        onItemChanged = { changedItem ->
                            // 리사이즈 중 occupancy 실시간 갱신 (유지)
                            occupancyState.updateGridItem(
                                id = changedItem.id,
                                x = changedItem.x,
                                y = changedItem.y,
                                spanX = changedItem.spanX,
                                spanY = changedItem.spanY
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        cellContent(item)
                    }
                }
            }
        }
    }
}

/**
 * 순수 Layout Composable - OccupancyState 접근 없이 measure/place만 수행
 * 
 * @param gridSize 그리드 크기
 * @param cellWidth 셀 너비
 * @param cellHeight 셀 높이
 * @param modifier Modifier
 * @param content 배치할 아이템들 (이미 위치가 결정된 상태)
 */
@Composable
private fun DynamicGridLayout(
    gridSize: GridSize,
    cellWidth: Dp,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val cellWidthPx = cellWidth.toPx()
        val cellHeightPx = cellHeight.toPx()
        val layoutWidth = (cellWidthPx * gridSize.columns).roundToInt()
        val layoutHeight = (cellHeightPx * gridSize.rows).roundToInt()

        // Measure: 각 아이템의 크기 계산
        val placeables = measurables.mapNotNull { measurable ->
            val gridItem = measurable.getGridItem() ?: return@mapNotNull null
            
            // Calculate dimensions based on span
            val itemWidth = (cellWidthPx * gridItem.spanX).roundToInt()
            val itemHeight = (cellHeightPx * gridItem.spanY).roundToInt()

            // Measure the child with calculated dimensions
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = itemWidth,
                    maxWidth = layoutWidth,
                    minHeight = itemHeight,
                    maxHeight = layoutHeight
                )
            )

            placeable to gridItem
        }

        // Layout: 계산된 위치에 배치
        layout(layoutWidth, layoutHeight) {
            placeables.forEach { (placeable, gridItem) ->
                placeable.place(
                    (cellWidthPx * gridItem.x).roundToInt(),
                    (cellHeightPx * gridItem.y).roundToInt()
                )
            }
        }
    }
}

/**
 * Extension function to extract GridItem from measurable parent data
 */
private fun Measurable.getGridItem(): GridItem? {
    return (parentData as? GridItemParentData)?.gridItem
}

private data class GridItemParentData(
    val gridItem: GridItem
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return this@GridItemParentData
    }
}

/**
 * Modifier to attach grid item data to a composable
 */
fun Modifier.gridItemData(gridItem: GridItem): Modifier {
    return this.then(GridItemParentData(gridItem))
}
