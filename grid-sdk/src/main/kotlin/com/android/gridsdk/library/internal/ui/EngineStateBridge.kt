package com.android.gridsdk.library.internal.ui

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.internal.state.RelocatedItemTracker
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine

/**
 * 엔진 결과를 UI 상태로 반영하는 브리지
 *
 * - EngineResult.Success → applyTo → onItemsChange
 * - Move/Resize 성공 시 RelocatedItemTracker에 재배치된 아이템 원위치 등록
 * - 드래그 중 evaluateRollback 호출로 복귀 적용
 * - 드래그/리사이즈 종료 시 tracker 초기화
 */
@InternalApi
internal class EngineStateBridge(
    private val onItemsChange: (List<GridItem>) -> Unit,
    private val onFailure: ((error: com.android.gridsdk.library.model.GridError) -> Unit)? = null
) {
    private val relocatedTracker = RelocatedItemTracker()

    /**
     * EngineResult.Success를 적용하고 onItemsChange를 호출합니다.
     * 재배치된 아이템의 원래 위치를 RelocatedItemTracker에 등록합니다.
     */
    internal fun applySuccess(
        result: EngineResult.Success,
        originalItems: List<GridItem>,
        gridSize: GridSize
    ) {
        val newItems = result.applyTo(originalItems)
        val relocatedWithOriginals = result.relocatedItems.mapNotNull { relocated ->
            val original = originalItems.find { it.id == relocated.id }
            if (original != null && (relocated.x != original.x || relocated.y != original.y)) {
                relocated.id to original
            } else {
                null
            }
        }.toMap()
        if (relocatedWithOriginals.isNotEmpty()) {
            relocatedTracker.addRelocated(relocatedWithOriginals)
        }
        onItemsChange(newItems)
        applyRollback(newItems, gridSize)
    }

    /**
     * Move + Resize 성공 결과를 한 번에 적용하여 onItemsChange를 1회만 호출합니다.
     * TopEnd/TopStart/BottomStart 리사이즈에서 재구성 횟수를 줄여 실시간 드래그 체감을 개선합니다.
     *
     * @param moveSuccess Move 엔진 성공 결과
     * @param resizeSuccess Resize 엔진 성공 결과 (moveSuccess.applyTo(originalItems) 기준)
     * @param originalItems 리사이즈 시작 시점의 아이템 목록
     * @param gridSize 그리드 크기
     * @param resizedItemId 리사이즈 대상 아이템 ID
     */
    internal fun applySuccessBatched(
        moveSuccess: EngineResult.Success,
        resizeSuccess: EngineResult.Success,
        originalItems: List<GridItem>,
        gridSize: GridSize,
        resizedItemId: String
    ) {
        val newItems = moveSuccess.applyTo(originalItems)
        val finalItems = resizeSuccess.applyTo(newItems)
        val targetItem = finalItems.find { it.id == resizedItemId }!!
        val relocatedIds = (moveSuccess.relocatedItems.map { it.id } + resizeSuccess.relocatedItems.map { it.id })
            .distinct()
            .filter { it != resizedItemId }
        val combinedRelocated = relocatedIds.mapNotNull { id -> finalItems.find { it.id == id } }
        val batchedResult = EngineResult.success(targetItem, combinedRelocated)
        val relocatedWithOriginals = batchedResult.relocatedItems.mapNotNull { relocated ->
            val original = originalItems.find { it.id == relocated.id }
            if (original != null && (relocated.x != original.x || relocated.y != original.y)) {
                relocated.id to original
            } else {
                null
            }
        }.toMap()
        if (relocatedWithOriginals.isNotEmpty()) {
            relocatedTracker.addRelocated(relocatedWithOriginals)
        }
        onItemsChange(finalItems)
        applyRollback(finalItems, gridSize)
    }

    /**
     * EngineResult.Failure 시 기존 상태 유지, onFailure 콜백 호출
     */
    internal fun applyFailure(result: EngineResult.Failure) {
        onFailure?.invoke(result.error)
    }

    /**
     * 드래그 중 롤백 판정을 수행하고, 복귀 가능 시 onItemsChange를 호출합니다.
     *
     * @return 롤백 적용된 아이템 목록 (호출자가 상태로 사용 가능)
     */
    internal fun applyRollback(
        currentItems: List<GridItem>,
        gridSize: GridSize
    ): List<GridItem> {
        val originals = relocatedTracker.getOriginals()
        if (originals.isEmpty()) return currentItems
        val rolledBack = GridEngine.evaluateRollback(currentItems, originals, gridSize)
        if (rolledBack != currentItems) {
            val rolledBackIds = originals.keys.filter { itemId ->
                val original = originals[itemId]!!
                val current = rolledBack.find { it.id == itemId }
                current != null && current.x == original.x && current.y == original.y
            }
            relocatedTracker.removeRolledBack(rolledBackIds)
            onItemsChange(rolledBack)
        }
        return rolledBack
    }

    /**
     * RelocatedItemTracker의 원래 위치 맵을 반환합니다.
     */
    internal fun getRelocatedOriginals(): Map<String, GridItem> = relocatedTracker.getOriginals()

    /**
     * 드래그/리사이즈 종료 시 추적 상태를 초기화합니다.
     */
    internal fun clearTracker() {
        relocatedTracker.clear()
    }
}
