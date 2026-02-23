package com.android.gridsdk.library.internal.resize

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp

interface ResizeStrategy {
    fun onResizeStart(offset: Offset)

    fun onResize(
        deltaW: Dp, deltaH: Dp, dragAmount: Offset,
        onContentUpdate: (Int, Int) -> Unit
    )

    fun onResizeEnd(
        onContentUpdate: (Int, Int) -> Unit
    )
}