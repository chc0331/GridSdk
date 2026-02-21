package com.android.gridsdk.library.model

/**
 * 그리드의 크기를 나타내는 데이터 클래스
 *
 * @property rows 행(N)의 개수 (세로 방향)
 * @property columns 열(M)의 개수 (가로 방향)
 * @throws IllegalArgumentException rows 또는 columns가 1보다 작은 경우
 */
public data class GridSize(
    val rows: Int,
    val columns: Int
) {
    init {
        require(rows > 0) { "Rows must be greater than 0, but was $rows" }
        require(columns > 0) { "Columns must be greater than 0, but was $columns" }
    }

    /**
     * 전체 셀의 개수를 반환합니다.
     */
    public val totalCells: Int
        get() = rows * columns

    /**
     * 주어진 행과 열 인덱스가 유효한 범위 내에 있는지 확인합니다.
     *
     * @param row 행 인덱스 (0-based)
     * @param column 열 인덱스 (0-based)
     * @return 유효한 범위 내에 있으면 true, 그렇지 않으면 false
     */
    public fun isValidPosition(row: Int, column: Int): Boolean {
        return row in 0 until rows && column in 0 until columns
    }

    /**
     * 주어진 좌표와 span이 그리드 범위를 벗어나지 않는지 확인합니다.
     *
     * @param row 시작 행 인덱스
     * @param column 시작 열 인덱스
     * @param rowSpan 행 방향 span
     * @param columnSpan 열 방향 span
     * @return 범위 내에 있으면 true, 그렇지 않으면 false
     */
    public fun isWithinBounds(
        row: Int,
        column: Int,
        rowSpan: Int,
        columnSpan: Int
    ): Boolean {
        return row >= 0 &&
                column >= 0 &&
                row + rowSpan <= rows &&
                column + columnSpan <= columns
    }

    public companion object {
        /**
         * 기본 그리드 크기 (4x4)
         */
        public val DEFAULT: GridSize = GridSize(rows = 4, columns = 4)

        /**
         * 일반적인 런처 그리드 크기 (5x4)
         */
        public val LAUNCHER_STANDARD: GridSize = GridSize(rows = 5, columns = 4)
    }
}
