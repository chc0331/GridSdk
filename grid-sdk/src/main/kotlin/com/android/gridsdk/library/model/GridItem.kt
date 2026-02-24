package com.android.gridsdk.library.model

/**
 * 그리드 내 아이템을 나타내는 데이터 클래스
 *
 * @property id 아이템의 고유 식별자
 * @property x 아이템의 시작 열(column) 위치 (0-based)
 * @property y 아이템의 시작 행(row) 위치 (0-based)
 * @property spanX 아이템이 차지하는 열(column) 개수
 * @property spanY 아이템이 차지하는 행(row) 개수
 * @throws IllegalArgumentException spanX 또는 spanY가 1보다 작은 경우
 */
public data class GridItem internal constructor(
    val id: String,
    val x: Int,
    val y: Int,
    val spanX: Int,
    val spanY: Int
) {
    init {
        require(spanX > 0) { "spanX must be greater than 0, but was $spanX" }
        require(spanY > 0) { "spanY must be greater than 0, but was $spanY" }
    }

    /**
     * 아이템의 끝 x 좌표 (exclusive)
     */
    public val endX: Int
        get() = x + spanX

    /**
     * 아이템의 끝 y 좌표 (exclusive)
     */
    public val endY: Int
        get() = y + spanY

    /**
     * 아이템이 차지하는 총 셀의 개수
     */
    public val area: Int
        get() = spanX * spanY

    /**
     * 주어진 그리드 크기 내에서 이 아이템이 유효한 위치에 있는지 확인합니다.
     *
     * @param gridSize 그리드 크기
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public fun isValidIn(gridSize: GridSize): Boolean {
        return x >= 0 &&
                y >= 0 &&
                gridSize.isWithinBounds(y, x, spanY, spanX)
    }

    /**
     * 주어진 셀 위치가 이 아이템이 차지하는 영역 내에 있는지 확인합니다.
     *
     * @param cellX 셀의 x 좌표
     * @param cellY 셀의 y 좌표
     * @return 아이템 영역 내에 있으면 true, 그렇지 않으면 false
     */
    public fun occupiesCell(cellX: Int, cellY: Int): Boolean {
        return cellX in x until endX && cellY in y until endY
    }

    /**
     * 이 아이템과 다른 아이템이 겹치는지 확인합니다.
     *
     * @param other 다른 아이템
     * @return 겹치면 true, 그렇지 않으면 false
     */
    public fun overlapsWith(other: GridItem): Boolean {
        return !(endX <= other.x || x >= other.endX ||
                endY <= other.y || y >= other.endY)
    }

    /**
     * 아이템의 위치를 변경한 새로운 인스턴스를 반환합니다.
     *
     * @param newX 새로운 x 좌표
     * @param newY 새로운 y 좌표
     * @return 새로운 위치의 GridItem 인스턴스
     */
    public fun moveTo(newX: Int, newY: Int): GridItem {
        return copy(x = newX, y = newY)
    }

    /**
     * 아이템의 span을 변경한 새로운 인스턴스를 반환합니다.
     *
     * @param newSpanX 새로운 spanX
     * @param newSpanY 새로운 spanY
     * @return 새로운 span의 GridItem 인스턴스
     */
    public fun resize(newSpanX: Int, newSpanY: Int): GridItem {
        return copy(spanX = newSpanX, spanY = newSpanY)
    }

    public companion object {
        /**
         * 1x1 크기의 기본 아이템을 생성합니다.
         *
         * @param id 아이템 ID
         * @param x x 좌표
         * @param y y 좌표
         * @return 1x1 GridItem
         */
        public fun single(id: String, x: Int, y: Int): GridItem {
            return GridItem(id, x, y, spanX = 1, spanY = 1)
        }

        fun create(id: String, spanX: Int, spanY: Int): GridItem {
            return GridItem(id = id, x = 0, y = 0, spanX = spanX, spanY = spanY)
        }
    }
}
