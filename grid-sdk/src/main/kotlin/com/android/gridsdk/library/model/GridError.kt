package com.android.gridsdk.library.model

/**
 * 그리드 작업 실패 시 반환되는 에러 타입
 *
 * Sealed class로 정의하여 가능한 모든 실패 케이스를 명시적으로 표현합니다.
 */
public sealed class GridError {

    /**
     * 아이템이 그리드 경계를 벗어나는 경우
     *
     * @property itemId 경계를 벗어난 아이템 ID
     * @property position 시도한 위치 정보
     * @property gridSize 그리드 크기
     */
    public data class OutOfBounds(
        val itemId: String,
        val position: Position,
        val gridSize: GridSize
    ) : GridError() {
        override fun toString(): String {
            return "OutOfBounds: Item '$itemId' at position $position exceeds grid bounds (${gridSize.rows}x${gridSize.columns})"
        }
    }

    /**
     * 배치 가능한 유효한 레이아웃이 없는 경우
     *
     * @property itemId 배치하려는 아이템 ID
     * @property reason 실패 이유
     * @property conflictingItems 충돌하는 아이템 ID 목록 (있는 경우)
     */
    public data class NoFeasibleLayout(
        val itemId: String,
        val reason: String,
        val conflictingItems: List<String> = emptyList()
    ) : GridError() {
        override fun toString(): String {
            val conflicts = if (conflictingItems.isNotEmpty()) {
                " (conflicts with: ${conflictingItems.joinToString(", ")})"
            } else ""
            return "NoFeasibleLayout: Cannot place item '$itemId' - $reason$conflicts"
        }
    }

    /**
     * 아이템이 다른 아이템과 겹치는 경우
     *
     * @property itemId 배치하려는 아이템 ID
     * @property overlappingItems 겹치는 아이템 ID 목록
     */
    public data class ItemOverlap(
        val itemId: String,
        val overlappingItems: List<String>
    ) : GridError() {
        override fun toString(): String {
            return "ItemOverlap: Item '$itemId' overlaps with: ${overlappingItems.joinToString(", ")}"
        }
    }

    /**
     * 아이템이 존재하지 않는 경우
     *
     * @property itemId 찾을 수 없는 아이템 ID
     */
    public data class ItemNotFound(
        val itemId: String
    ) : GridError() {
        override fun toString(): String {
            return "ItemNotFound: Item '$itemId' does not exist in the grid"
        }
    }

    /**
     * 유효하지 않은 아이템 구성 (예: 음수 좌표, 0 이하의 span)
     *
     * @property itemId 유효하지 않은 아이템 ID
     * @property reason 유효하지 않은 이유
     */
    public data class InvalidItem(
        val itemId: String,
        val reason: String
    ) : GridError() {
        override fun toString(): String {
            return "InvalidItem: Item '$itemId' is invalid - $reason"
        }
    }

    /**
     * 그리드가 이미 가득 찬 경우
     *
     * @property gridSize 그리드 크기
     */
    public data class GridFull(
        val gridSize: GridSize
    ) : GridError() {
        override fun toString(): String {
            return "GridFull: Grid (${gridSize.rows}x${gridSize.columns}) has no available space"
        }
    }

    /**
     * 중복된 아이템 ID
     *
     * @property itemId 중복된 아이템 ID
     */
    public data class DuplicateItemId(
        val itemId: String
    ) : GridError() {
        override fun toString(): String {
            return "DuplicateItemId: Item ID '$itemId' already exists"
        }
    }

    /**
     * 위치 정보를 담는 데이터 클래스
     */
    public data class Position(
        val x: Int,
        val y: Int,
        val spanX: Int,
        val spanY: Int
    ) {
        override fun toString(): String {
            return "($x, $y) with span ($spanX, $spanY)"
        }
    }
}
