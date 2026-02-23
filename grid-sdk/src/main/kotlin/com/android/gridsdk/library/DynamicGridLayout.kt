package com.android.gridsdk.library

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.android.gridsdk.library.internal.ui.DynamicGridItemLayout
import com.android.gridsdk.library.internal.ui.LocalGridCellSize
import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

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
public fun DynamicGridLayout(
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

        CompositionLocalProvider(
            LocalGridCellSize provides DpSize(cellWidth, cellHeight)
        ) {
            var currentResizeItemId by remember { mutableStateOf<String?>(null) }

            Box(modifier = Modifier.fillMaxSize()) {
                items.forEach { item ->
                    DynamicGridItemLayout(
                        modifier = Modifier.offset(
                            x = cellWidth * item.x,
                            y = cellHeight * item.y
                        ),
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
                        cellContent = cellContent
                    )
                }
            }
        }
    }
}
