package com.android.gridsdk.library.internal.resize

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.max

class BottomEndStrategy(
    private val initialSize: DpSize,
    private val initialSpanX: Int,
    private val initialSpanY: Int,
    private val cellWidth: Dp,
    private val cellHeight: Dp,
) : ResizeStrategy {

    var spanX by mutableIntStateOf(initialSpanX)
    var spanY by mutableIntStateOf(initialSpanY)

    var dragStartOffset by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)

    var rawSize by mutableStateOf(initialSize)
    var contentSize by mutableStateOf(initialSize)

    override fun onResizeStart(offset: Offset) {
        dragStartOffset = offset
        dragOffset = offset
    }

    override fun onResize(
        deltaW: Dp,
        deltaH: Dp,
        dragAmount: Offset,
        onContentUpdate: (Int, Int) -> Unit
    ) {
        dragOffset += Offset(dragAmount.x, dragAmount.y)

        val currentRawSize = rawSize
        val nextRawSize = DpSize(currentRawSize.width + deltaW, currentRawSize.height + deltaH)
        rawSize = nextRawSize

        val widthIncreasing = dragOffset.x > dragStartOffset.x
        val heightIncreasing = dragOffset.y > dragStartOffset.y
        val (nextSpanX, nextSpanY) = SpanCalculator.calculateNextSpan(
            nextRawSize.width,
            nextRawSize.height,
            cellWidth,
            cellHeight,
            widthIncreasing,
            heightIncreasing
        )
        val isChanged = (spanX != nextSpanX) or (spanY != nextSpanY)
        if (isChanged) {

            // todo : check collision 로직 재설계 필요
//                    (containerSpanX != nextSpan.column) or (containerSpanY != nextSpan.row)
//                //1. Next Span 변경
//                if (isChanged) {
//                    // 2. Next Span 위치에 다른 위젯들 있는지 확인
//                    if (checkCollision(widgetState, nextSpan)) {
//                        // todo
//                        // 2-1. 있으면 겹치는 위젯 리스트들을 받아옴.
//
//                        // 2-2. 겹치는 위젯들이 모두 이동할 공간이 있는지 확인.
//
//                        // 2-3. 모두 이동할 공간이 없으면 사이즈를 commit하지 않음.
//
//                        // 2-4 모두 이동할 공간이 있으면 겹치는 위젯들의 x,y 좌표를 업데이트함.
//
//                        // 2-5. 리사이즈 중인 위젯이 다시 예전 사이즈로 돌아간다면 이동시켰던 위젯들 위치를 롤백.
//                    } else {
//                        // 2-6. 없으면 바로 리사이즈

            if (nextSpanX < 1 || nextSpanY < 1) return
            dragStartOffset = dragOffset
            spanX = nextSpanX
            spanY = nextSpanY
            val updateContentSize = DpSize(
                (spanX * cellWidth.value).dp,
                (spanY * cellHeight.value).dp
            )
            contentSize = updateContentSize
            onContentUpdate(spanX, spanY)
        }
    }

    override fun onResizeEnd(
        onContentUpdate: (Int, Int) -> Unit
    ) {
        val widthIncreasing = dragOffset.x > dragStartOffset.x
        val heightIncreasing = dragOffset.y > dragStartOffset.y
        val currentRawSize = rawSize

        val (nextSpanX, nextSpanY) = SpanCalculator.calculateNextSpan(
            currentRawSize.width,
            currentRawSize.height,
            cellWidth,
            cellHeight,
            widthIncreasing,
            heightIncreasing
        )

        spanX = max(nextSpanX, 1)
        spanY = max(nextSpanY, 1)

        val newSize = DpSize(
            (cellWidth * spanX),
            (cellHeight * spanY)
        )
        rawSize = newSize
        contentSize = newSize

        onContentUpdate(spanX, spanY)
        dragStartOffset = Offset.Zero
    }
}