package com.android.gridsdk.library

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.android.gridsdk.library.internal.ui.GridLayoutContent
import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import kotlin.math.max

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
public fun GridLayout(
    gridSize: GridSize,
    items: List<GridItem>,
    onItemsChange: (List<GridItem>) -> Unit,
    modifier: Modifier = Modifier,
    onFailure: ((GridError) -> Unit)? = null,
    cellContent: @Composable (GridItem) -> Unit = {}
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cellWidthPx = with(density) { maxWidth.toPx() } / gridSize.columns
        val cellHeightPx = with(density) { maxHeight.toPx() } / gridSize.rows
        GridLayoutContent(
            gridSize = gridSize,
            items = items,
            onItemsChange = onItemsChange,
            cellWidthPx = cellWidthPx,
            cellHeightPx = cellHeightPx,
            onFailure = onFailure,
            cellContent = cellContent
        )
    }
}
