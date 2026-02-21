package com.android.gridsdk.library.internal.state

import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem

/**
 * 드래그 중 임시 재배치된 아이템의 원래 위치를 추적하는 구조
 *
 * Move/Resize 성공 시 재배치된 아이템의 원래 위치를 등록하고,
 * 포인터 이동마다 롤백 가능 여부 판정 시 사용합니다.
 * PRD 롤백 규칙: "드래그 중 재배치되었던 아이템은, 본래 위치가 다시 유효해지면 원위치로 복귀"
 */
@InternalApi
internal class RelocatedItemTracker {

    /**
     * itemId -> 원래 위치의 GridItem
     */
    private val originals: MutableMap<String, GridItem> = mutableMapOf()

    /**
     * Move/Resize 성공 시 재배치된 아이템의 원래 위치를 등록합니다.
     *
     * @param itemId 재배치된 아이템 ID
     * @param originalItem 원래 위치의 GridItem
     */
    internal fun addRelocated(itemId: String, originalItem: GridItem) {
        originals[itemId] = originalItem
    }

    /**
     * 여러 재배치된 아이템의 원래 위치를 일괄 등록합니다.
     *
     * @param relocatedWithOriginals itemId -> 원래 위치의 GridItem
     */
    internal fun addRelocated(relocatedWithOriginals: Map<String, GridItem>) {
        originals.putAll(relocatedWithOriginals)
    }

    /**
     * 복귀 완료된 아이템들을 추적 대상에서 제거합니다.
     *
     * @param itemIds 복귀된 아이템 ID 목록
     */
    internal fun removeRolledBack(itemIds: Iterable<String>) {
        itemIds.forEach { originals.remove(it) }
    }

    /**
     * 롤백 판정 시 사용할 원래 위치 맵을 반환합니다.
     *
     * @return itemId -> 원래 위치의 GridItem (읽기 전용)
     */
    internal fun getOriginals(): Map<String, GridItem> = originals.toMap()

    /**
     * 드래그 종료 시 추적 상태를 초기화합니다.
     */
    internal fun clear() {
        originals.clear()
    }
}
