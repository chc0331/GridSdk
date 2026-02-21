package com.android.gridsdk.library.internal.state

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.engine.EngineResult

/**
 * 최종 레이아웃 적용/롤백이 가능한 상태 스냅샷
 *
 * 배치 엔진의 성공 결과를 적용하거나, 적용 후 이전 상태로 롤백할 수 있습니다.
 * [EngineResult.Success]와 연동하여 사용합니다.
 *
 * @property previousItems 적용 전 아이템 목록 (롤백 시 복원되는 상태)
 * @property newItems 적용 후 아이템 목록 (적용 시 사용되는 상태)
 */
@InternalApi
internal data class StateSnapshot(
    val previousItems: List<GridItem>,
    val newItems: List<GridItem>
) {
    /**
     * 새 레이아웃을 적용한 아이템 목록을 반환합니다.
     */
    internal fun apply(): List<GridItem> = newItems

    /**
     * 이전 상태로 롤백한 아이템 목록을 반환합니다.
     */
    internal fun rollback(): List<GridItem> = previousItems

    internal companion object {
        /**
         * [EngineResult.Success]로부터 [StateSnapshot]을 생성합니다.
         *
         * @param originalItems 요청 시점의 아이템 목록
         * @param success 배치 엔진 성공 결과
         * @return 적용/롤백 가능한 스냅샷
         */
        internal fun fromSuccess(
            originalItems: List<GridItem>,
            success: EngineResult.Success
        ): StateSnapshot {
            val newItems = success.applyTo(originalItems)
            return StateSnapshot(previousItems = originalItems, newItems = newItems)
        }
    }
}
