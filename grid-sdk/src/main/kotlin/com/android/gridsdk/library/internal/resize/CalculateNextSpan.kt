package com.android.gridsdk.library.internal.resize

import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.floor

object SpanCalculator {
    /**
     * 다음 Span 계산 로직.
     *
     * 1) Width
     *
     * 1-1) Width가 증가할 경우
     * Span = floor(Width / cell width) => 내림
     * 1-2) Width가 감소할 경우
     * Span = ceil(width / cell width) => 올림
     * */
    fun calculateNextSpan(
        currentWidth: Dp, currentHeight: Dp,
        cellWidth: Dp, cellHeight: Dp,
        isWidthIncreased: Boolean, isHeightIncreased: Boolean
    ): Pair<Int, Int> {
        val widthThreshold = cellWidth * 0.22f
        val heightThreshold = cellHeight * 0.22f
        // Width
        val newSpanX = if (isWidthIncreased) {
            floor((currentWidth + widthThreshold) / cellWidth).toInt()
        } else {
            ceil((currentWidth - widthThreshold) / cellWidth).toInt()
        }
        // Height
        val newSpanY = if (isHeightIncreased) {
            floor((currentHeight + heightThreshold) / cellHeight).toInt()
        } else {
            ceil((currentHeight - heightThreshold) / cellHeight).toInt()
        }
        return newSpanX to newSpanY
    }
}