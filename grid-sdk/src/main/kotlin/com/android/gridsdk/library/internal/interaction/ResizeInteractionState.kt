package com.android.gridsdk.library.internal.interaction

import com.android.gridsdk.library.internal.InternalApi

/**
 * LongPress → Drag 리사이즈 진입을 위한 상호작용 상태 머신
 *
 * PRD: "Long Press -> Drag로 아이템 사이즈 변경"
 *
 * 상태 전이:
 * - IDLE → PRESSED (pointer down)
 * - PRESSED → DRAG_MOVE (pointer move before long press threshold)
 * - PRESSED → LONG_PRESSED (long press threshold reached)
 * - LONG_PRESSED → RESIZE_DRAG (pointer move after long press)
 * - DRAG_MOVE → IDLE (pointer up)
 * - RESIZE_DRAG → IDLE (pointer up)
 *
 * 8단계 Compose 제스처 핸들러에서 사용됩니다.
 */
@InternalApi
internal enum class ResizeInteractionState {

    /** 초기 상태, 터치 없음 */
    IDLE,

    /** 포인터 다운, long press 대기 중 */
    PRESSED,

    /** long press 임계값 도달, 리사이즈 모드 진입 대기 */
    LONG_PRESSED,

    /** 일반 드래그 (이동 모드) */
    DRAG_MOVE,

    /** long press 후 드래그 (리사이즈 모드) */
    RESIZE_DRAG;

    /**
     * 현재 상태에서 리사이즈 모드인지 여부
     */
    internal val isResizeMode: Boolean
        get() = this == RESIZE_DRAG

    /**
     * 현재 상태에서 이동 모드인지 여부
     */
    internal val isMoveMode: Boolean
        get() = this == DRAG_MOVE

    /**
     * 포인터 업 시 전이할 상태
     */
    internal fun onPointerUp(): ResizeInteractionState = IDLE

    /**
     * long press 임계값 도달 시 전이
     */
    internal fun onLongPress(): ResizeInteractionState = when (this) {
        PRESSED -> LONG_PRESSED
        else -> this
    }

    /**
     * 포인터 이동 시 전이
     * - PRESSED에서 이동: long press 전 → DRAG_MOVE (이동 모드)
     * - LONG_PRESSED에서 이동: long press 후 → RESIZE_DRAG (리사이즈 모드)
     */
    internal fun onPointerMove(): ResizeInteractionState = when (this) {
        PRESSED -> DRAG_MOVE
        LONG_PRESSED -> RESIZE_DRAG
        else -> this
    }
}
