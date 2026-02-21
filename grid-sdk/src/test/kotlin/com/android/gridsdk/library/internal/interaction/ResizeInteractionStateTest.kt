package com.android.gridsdk.library.internal.interaction

import org.junit.Assert.*
import org.junit.Test

class ResizeInteractionStateTest {

    @Test
    fun `IDLE onPointerUp stays IDLE`() {
        assertEquals(ResizeInteractionState.IDLE, ResizeInteractionState.IDLE.onPointerUp())
    }

    @Test
    fun `PRESSED onPointerUp returns IDLE`() {
        assertEquals(ResizeInteractionState.IDLE, ResizeInteractionState.PRESSED.onPointerUp())
    }

    @Test
    fun `PRESSED onLongPress returns LONG_PRESSED`() {
        assertEquals(
            ResizeInteractionState.LONG_PRESSED,
            ResizeInteractionState.PRESSED.onLongPress()
        )
    }

    @Test
    fun `PRESSED onPointerMove returns DRAG_MOVE`() {
        assertEquals(
            ResizeInteractionState.DRAG_MOVE,
            ResizeInteractionState.PRESSED.onPointerMove()
        )
    }

    @Test
    fun `LONG_PRESSED onPointerMove returns RESIZE_DRAG`() {
        assertEquals(
            ResizeInteractionState.RESIZE_DRAG,
            ResizeInteractionState.LONG_PRESSED.onPointerMove()
        )
    }

    @Test
    fun `RESIZE_DRAG isResizeMode is true`() {
        assertTrue(ResizeInteractionState.RESIZE_DRAG.isResizeMode)
    }

    @Test
    fun `DRAG_MOVE isMoveMode is true`() {
        assertTrue(ResizeInteractionState.DRAG_MOVE.isMoveMode)
    }

    @Test
    fun `RESIZE_DRAG onPointerUp returns IDLE`() {
        assertEquals(ResizeInteractionState.IDLE, ResizeInteractionState.RESIZE_DRAG.onPointerUp())
    }
}
