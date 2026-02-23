package com.android.gridsdk.library.internal.resize

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.android.gridsdk.library.model.GridItem

abstract class ResizeStrategy(
    private val initialSize: DpSize,
    private val initialSpanX: Int,
    private val initialSpanY: Int,
    private val cellWidth: Dp,
    private val cellHeight: Dp,
) {

    var spanX by mutableIntStateOf(initialSpanX)
    var spanY by mutableIntStateOf(initialSpanY)

    var dragStartOffset by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)

    var rawSize by mutableStateOf(initialSize)
    var contentSize by mutableStateOf(initialSize)

    abstract fun onResizeStart(offset: Offset)

    abstract fun onResize(
        item: GridItem, deltaW: Dp, deltaH: Dp, dragAmount: Offset,
        onContentUpdate: (spanX: Int, spanY: Int, offsetX: Int, offsetY: Int) -> Unit
    )

    abstract fun onResizeEnd(
        onContentUpdate: (Int, Int) -> Unit
    )
}