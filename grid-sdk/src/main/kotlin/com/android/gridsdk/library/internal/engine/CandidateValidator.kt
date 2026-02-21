package com.android.gridsdk.library.internal.engine

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.util.ValidationUtils

/**
 * 후보 레이아웃의 유효성 검사
 *
 * PRD 공통 제약:
 * - 모든 아이템은 그리드 경계를 벗어나면 안 된다.
 * - 아이템 간 셀 중복 점유는 허용하지 않는다.
 */
@InternalApi
internal object CandidateValidator {

    /**
     * 후보 아이템 목록이 유효한지 검사합니다.
     *
     * 유효 조건:
     * - 모든 아이템이 그리드 경계 내에 있음
     * - 아이템 간 겹침(중복 점유)이 없음
     *
     * @param items 검사할 아이템 목록
     * @param gridSize 그리드 크기
     * @return 유효하면 true, 그렇지 않으면 false
     */
    internal fun isValidCandidate(items: List<GridItem>, gridSize: GridSize): Boolean {
        if (items.isEmpty()) return true
        return ValidationUtils.allWithinBounds(items, gridSize) &&
                !ValidationUtils.hasAnyOverlap(items)
    }
}
