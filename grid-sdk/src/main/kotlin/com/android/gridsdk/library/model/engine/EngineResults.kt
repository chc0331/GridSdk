package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem

/**
 * 배치 엔진 결과 DTO
 *
 * PRD 출력 스펙:
 * - 성공: 대상 아이템 최종 위치/크기 + 재배치된 아이템들의 최종 위치
 * - 실패: 기존 상태 유지(이동/리사이즈 미적용)
 */
public sealed class EngineResult {

    /**
     * 성공 결과
     *
     * @property targetItem 대상 아이템의 최종 상태 (이동/리사이즈/추가된 아이템)
     * @property relocatedItems 재배치된 아이템들의 최종 위치 (목록 내 각 아이템은 새 x, y, span 유지)
     */
    public data class Success(
        val targetItem: GridItem,
        val relocatedItems: List<GridItem>
    ) : EngineResult() {

        /**
         * 적용 후 전체 아이템 목록을 반환합니다.
         * 원본 목록에서 targetItem 및 relocatedItems로 교체한 결과입니다.
         *
         * @param originalItems 요청 시점의 아이템 목록
         * @return targetItem과 relocatedItems가 반영된 최종 목록
         */
        public fun applyTo(originalItems: List<GridItem>): List<GridItem> {
            val replacedIds = (listOf(targetItem) + relocatedItems).map { it.id }.toSet()
            val withoutReplaced = originalItems.filter { it.id !in replacedIds }
            return withoutReplaced + targetItem + relocatedItems
        }
    }

    /**
     * 실패 결과
     *
     * 기존 상태를 유지하며, 이동/리사이즈/추가가 적용되지 않습니다.
     *
     * @property error 실패 원인
     */
    public data class Failure(
        val error: GridError
    ) : EngineResult()

    public companion object {
        public fun success(targetItem: GridItem, relocatedItems: List<GridItem> = emptyList()): Success =
            Success(targetItem, relocatedItems)

        public fun failure(error: GridError): Failure = Failure(error)
    }
}

/** [EngineResult.Success] 타입 별칭 */
public typealias EngineSuccess = EngineResult.Success

/** [EngineResult.Failure] 타입 별칭 */
public typealias EngineFailure = EngineResult.Failure
