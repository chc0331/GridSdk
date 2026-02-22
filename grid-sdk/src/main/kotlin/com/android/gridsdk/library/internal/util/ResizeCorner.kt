package com.android.gridsdk.library.internal.util

import com.android.gridsdk.library.internal.InternalApi

/**
 * 리사이즈 핸들러의 코너 위치
 *
 * - TopStart: 좌상단 핸들 (수평: right 고정, 수직: bottom 고정)
 * - TopEnd: 우상단 핸들 (수평: left 고정, 수직: bottom 고정)
 * - BottomStart: 좌하단 핸들 (수평: right 고정, 수직: top 고정)
 * - BottomEnd: 우하단 핸들 (수평: left 고정, 수직: top 고정)
 */
@InternalApi
internal enum class ResizeCorner {
    TopStart,
    TopEnd,
    BottomStart,
    BottomEnd
}
