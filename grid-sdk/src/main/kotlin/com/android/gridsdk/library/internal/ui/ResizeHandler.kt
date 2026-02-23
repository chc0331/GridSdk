package com.android.gridsdk.library.internal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.internal.util.ResizeCorner

// todo : 양 방향 Resize 기능 구현 필요
/**
 * 1. 4방향 핸들러는 각각 별개의 컴포저블로 제공되어야 함.
 * */
@Composable
internal fun ResizeHandler(
    type: ResizeCorner,
    size: DpSize,
    onResizeStart: (Offset) -> Unit,
    onResize: (Dp, Dp, Offset) -> Boolean,
    onResizeEnd: () -> Unit,
    modifier: Modifier = Modifier,
    outlineColor: Color = Color.LightGray
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.Transparent, RoundedCornerShape(18.dp))
            .border(2.dp, Color.White, RoundedCornerShape(18.dp))
    )

    Box(
        modifier = modifier
            .size(size)
            .drawHandler(outlineColor, type)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onResizeStart(offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaWidth = with(density) { dragAmount.x.toDp() }
                        val deltaHeight = with(density) { dragAmount.y.toDp() }
                        onResize(deltaWidth, deltaHeight, dragAmount)
                    },
                    onDragCancel = {

                    },
                    onDragEnd = {
                        onResizeEnd()
                    }
                )
            }
    )
}

fun Modifier.drawHandler(color: Color, type: ResizeCorner) =
    this then DrawHandlerElement(color, type)

private data class DrawHandlerElement(val color: Color, val type: ResizeCorner) :
    ModifierNodeElement<DrawHandlerNode>() {
    override fun create() = DrawHandlerNode(color, type)

    override fun update(node: DrawHandlerNode) {
        node.color = color
    }
}

private class DrawHandlerNode(var color: Color, var type: ResizeCorner) : DrawModifierNode,
    Modifier.Node() {
    override fun ContentDrawScope.draw() {
        when (type) {
            ResizeCorner.TopStart -> {
                drawTopStart(color)
            }

            ResizeCorner.TopEnd -> {
                drawTopEnd(color)
            }

            ResizeCorner.BottomStart -> {
                drawBottomStart(color)
            }

            ResizeCorner.BottomEnd -> {
                drawBottomEnd(color)
            }
        }
    }
}

private fun ContentDrawScope.drawTopStart(color: Color) {
    val strokeWidth = 8.dp.toPx()
    val radius = 18.dp.toPx() // 곡률 반지름
    val pathLength = 50.dp.toPx() // 모서리에서 이어질 직선의 길이

    val path = Path().apply {
        // 1. 왼쪽 상단 지점에서 시작 (직선 부분)
        moveTo(0f, pathLength)
        lineTo(0f, radius)
        arcTo(
            rect = Rect(
                left = 0f,
                top = 0f,
                right = 2 * radius,
                bottom = 2 * radius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(pathLength, 0f)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun ContentDrawScope.drawTopEnd(color: Color) {
    val strokeWidth = 8.dp.toPx()
    val radius = 18.dp.toPx() // 곡률 반지름
    val pathLength = 50.dp.toPx() // 모서리에서 이어질 직선의 길이

    val path = Path().apply {
        // 1. 오른쪽 상단 지점에서 시작 (직선 부분)
        moveTo(size.width - pathLength, 0f)
        lineTo(size.width - radius, 0f)
        arcTo(
            rect = Rect(
                left = size.width - 2 * radius,
                top = 0f,
                right = size.width,
                bottom = 2 * radius
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(size.width, pathLength)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun ContentDrawScope.drawBottomStart(color: Color) {
    val strokeWidth = 8.dp.toPx()
    val radius = 18.dp.toPx() // 곡률 반지름
    val pathLength = 50.dp.toPx() // 모서리에서 이어질 직선의 길이

    val path = Path().apply {
        // 1. 왼쪽 하단 지점에서 시작 (직선 부분)
        moveTo(0f, size.height - pathLength)
        lineTo(0f, size.height - radius)
        arcTo(
            rect = Rect(
                left = 0f,
                top = size.height - 2 * radius,
                right = 2 * radius,
                bottom = size.height
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )
        lineTo(pathLength, size.height)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}

private fun ContentDrawScope.drawBottomEnd(color: Color) {
    val strokeWidth = 8.dp.toPx()
    val radius = 18.dp.toPx() // 곡률 반지름
    val pathLength = 50.dp.toPx() // 모서리에서 이어질 직선의 길이

    val path = Path().apply {
        // 1. 오른쪽 하단 위쪽 지점에서 시작 (직선 부분)
        moveTo(size.width, size.height - pathLength)
        // 2. 곡선이 시작되기 전까지 직선 그리기
        lineTo(size.width, size.height - radius)
        // 3. 오른쪽 하단 모서리 곡선 그리기
        arcTo(
            rect = Rect(
                left = size.width - 2 * radius,
                top = size.height - 2 * radius,
                right = size.width,
                bottom = size.height
            ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        // 4. 하단 왼쪽으로 뻗어나가는 직선 그리기
        lineTo(size.width - pathLength, size.height)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )
}