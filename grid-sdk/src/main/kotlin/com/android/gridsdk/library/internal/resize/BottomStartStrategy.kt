package com.android.gridsdk.library.internal.resize

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.android.gridsdk.library.model.GridItem

class BottomStartStrategy(
    val initialSize: DpSize,
    val initialSpanX: Int,
    val initialSpanY: Int,
    val cellWidth: Dp,
    val cellHeight: Dp,
) : ResizeStrategy(initialSize, initialSpanX, initialSpanY, cellWidth, cellHeight) {

    override fun onResizeStart(offset: Offset) {
        dragStartOffset = offset
        dragOffset = offset
    }

    override fun onResize(
        item: GridItem,
        deltaW: Dp,
        deltaH: Dp,
        dragAmount: Offset,
        onContentUpdate: (Int, Int, Int, Int) -> Unit
    ) {

    }

    override fun onResizeEnd(
        onContentUpdate: (Int, Int) -> Unit
    ) {
    }
}