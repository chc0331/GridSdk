package com.android.gridsdk.library.internal.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.android.gridsdk.library.internal.InternalApi
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

@Composable
internal fun rememberOccupancyState(gridSize: GridSize): OccupancyState {
    return remember(gridSize) {
        OccupancyState(gridSize)
    }
}

internal class OccupancyState(gridSize: GridSize) {
    var currentGridSize: GridSize
    var occupancyMap by mutableStateOf<Array<Array<String?>>?>(null)
        private set

    init {
        currentGridSize = gridSize
        updateGridSize(gridSize)
    }

    fun updateGridSize(gridSize: GridSize) {
        currentGridSize = gridSize
        occupancyMap = Array(gridSize.rows) { Array(gridSize.columns) { null } }
    }

    fun updateGridItem(id: String, x: Int, y: Int, spanX: Int, spanY: Int) {
        remove(id)
        place(id, x, y, spanX, spanY)
    }

    fun place(id: String, x: Int, y: Int, spanX: Int, spanY: Int) {
        (x until x + spanX).forEach { i ->
            (y until y + spanY).forEach { j ->
                occupancyMap?.get(j)[i] = id
            }
        }
    }

    fun isEmpty(x: Int, y: Int): Boolean {
        if (!isValidCell(x, y)) return false
        return occupancyMap?.get(y)[x] == null
    }

    fun getOccupant(x: Int, y: Int): String? {
        if (!isValidCell(x, y)) return null
        return occupancyMap?.get(y)[x]
    }

    fun findAddPosition(spanX: Int, spanY: Int): Pair<Int, Int>? {
        // find top left empty position
        (0 until currentGridSize.rows).forEach { y ->
            (0 until currentGridSize.columns).forEach { x ->
                if (isValidCell(x + spanX - 1, y + spanY - 1) &&
                    isAreaEmpty(x, y, spanX, spanY)
                ) {
                    return x to y
                }
            }
        }
        return null
    }

    fun findPosition(id: String): Pair<Int, Int>? {
        (0 until currentGridSize.columns).forEach { x ->
            (0 until currentGridSize.rows).forEach { y ->
                if (occupancyMap?.get(y)[x] == id) return x to y
            }
        }
        return null
    }

    fun checkIdExist(id: String): Boolean {
        (0 until currentGridSize.rows).forEach { y ->
            (0 until currentGridSize.columns).forEach { x ->
                if (occupancyMap?.get(y)[x] == id) return true
            }
        }
        return false
    }

    fun remove(id: String) {
        (0 until currentGridSize.rows).forEach { y ->
            (0 until currentGridSize.columns).forEach { x ->
                if (occupancyMap?.get(y)[x] == id)
                    occupancyMap?.get(y)[x] = null
            }
        }
    }

    fun clear() {
        (0 until currentGridSize.rows).forEach { y ->
            (0 until currentGridSize.columns).forEach { x ->
                occupancyMap?.get(y)[x] = null
            }
        }
    }

    private fun isValidCell(x: Int, y: Int): Boolean {
        return x >= 0 && x < currentGridSize.columns && y >= 0 && y < currentGridSize.rows
    }

    private fun isAreaEmpty(x: Int, y: Int, spanX: Int, spanY: Int): Boolean {
        (x until x + spanX).forEach { i ->
            (y until y + spanY).forEach { j ->
                if (occupancyMap?.get(j)[i] != null) return false
            }
        }
        return true
    }

    override fun toString(): String {
        return buildString {
            appendLine("OccupancyGrid(${currentGridSize.columns}x${currentGridSize.rows}):")
            (0 until currentGridSize.rows).forEach { y ->
                (0 until currentGridSize.columns).forEach { x ->
                    append(occupancyMap?.get(y)[x])
                    append(" ")
                }
                appendLine()
            }
        }
    }
}

/**
 * 그리드 셀의 점유 상태를 추적하는 자료구조
 *
 * 각 셀이 어떤 아이템에 의해 점유되었는지를 효율적으로 관리합니다.
 * 내부 구현에서만 사용됩니다.
 *
 * @property gridSize 그리드 크기
 */
@InternalApi
internal class OccupancyGrid(
    private val gridSize: GridSize
) {
    /**
     * 2차원 배열로 각 셀의 점유 상태를 저장
     * grid[row][column] = 해당 셀을 점유한 아이템의 ID (null이면 빈 셀)
     */
    private val grid: Array<Array<String?>> = Array(gridSize.rows) {
        Array(gridSize.columns) { null }
    }

    /**
     * 특정 셀이 비어있는지 확인합니다.
     *
     * @param x 열 좌표
     * @param y 행 좌표
     * @return 비어있으면 true, 점유되었거나 범위 밖이면 false
     */
    internal fun isEmpty(x: Int, y: Int): Boolean {
        if (!isValidCell(x, y)) return false
        return grid[y][x] == null
    }

    /**
     * 특정 셀을 점유한 아이템 ID를 반환합니다.
     *
     * @param x 열 좌표
     * @param y 행 좌표
     * @return 아이템 ID, 비어있거나 범위 밖이면 null
     */
    internal fun getOccupant(x: Int, y: Int): String? {
        if (!isValidCell(x, y)) return null
        return grid[y][x]
    }

    /**
     * 아이템을 그리드에 배치합니다.
     *
     * @param item 배치할 아이템
     * @throws IllegalStateException 배치 영역이 이미 점유되어 있는 경우
     */
    internal fun place(item: GridItem) {
        require(isValidCell(item.x, item.y)) {
            "Item ${item.id} start position (${item.x}, ${item.y}) is out of bounds"
        }
        require(item.endX <= gridSize.columns && item.endY <= gridSize.rows) {
            "Item ${item.id} exceeds grid bounds: end position (${item.endX}, ${item.endY})"
        }

        // 영역이 비어있는지 확인
        for (row in item.y until item.endY) {
            for (col in item.x until item.endX) {
                val occupant = grid[row][col]
                check(occupant == null) {
                    "Cannot place item ${item.id}: cell ($col, $row) is already occupied by $occupant"
                }
            }
        }

        // 아이템 배치
        for (row in item.y until item.endY) {
            for (col in item.x until item.endX) {
                grid[row][col] = item.id
            }
        }
    }

    /**
     * 아이템을 그리드에서 제거합니다.
     *
     * @param item 제거할 아이템
     */
    internal fun remove(item: GridItem) {
        for (row in item.y until item.endY) {
            for (col in item.x until item.endX) {
                if (isValidCell(col, row) && grid[row][col] == item.id) {
                    grid[row][col] = null
                }
            }
        }
    }

    /**
     * 특정 아이템 ID를 가진 모든 셀을 제거합니다.
     *
     * @param itemId 제거할 아이템 ID
     */
    internal fun removeById(itemId: String) {
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                if (grid[row][col] == itemId) {
                    grid[row][col] = null
                }
            }
        }
    }

    /**
     * 아이템이 배치 가능한지 확인합니다 (충돌하지 않는지).
     *
     * @param item 확인할 아이템
     * @param excludeItemId 무시할 아이템 ID (이동 시 자기 자신 제외용)
     * @return 배치 가능하면 true
     */
    internal fun canPlace(item: GridItem, excludeItemId: String? = null): Boolean {
        if (!isValidCell(item.x, item.y)) return false
        if (item.endX > gridSize.columns || item.endY > gridSize.rows) return false

        for (row in item.y until item.endY) {
            for (col in item.x until item.endX) {
                val occupant = grid[row][col]
                if (occupant != null && occupant != excludeItemId) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 아이템과 충돌하는 아이템 ID 목록을 반환합니다.
     *
     * @param item 확인할 아이템
     * @param excludeItemId 무시할 아이템 ID
     * @return 충돌하는 고유 아이템 ID 집합
     */
    internal fun getConflictingItems(item: GridItem, excludeItemId: String? = null): Set<String> {
        val conflicts = mutableSetOf<String>()

        if (!isValidCell(item.x, item.y)) return conflicts
        if (item.endX > gridSize.columns || item.endY > gridSize.rows) return conflicts

        for (row in item.y until item.endY) {
            for (col in item.x until item.endX) {
                val occupant = grid[row][col]
                if (occupant != null && occupant != excludeItemId) {
                    conflicts.add(occupant)
                }
            }
        }
        return conflicts
    }

    /**
     * 특정 영역이 비어있는지 확인합니다.
     *
     * @param x 시작 열 좌표
     * @param y 시작 행 좌표
     * @param spanX 열 방향 span
     * @param spanY 행 방향 span
     * @param excludeItemId 무시할 아이템 ID
     * @return 영역이 비어있으면 true
     */
    internal fun isAreaEmpty(
        x: Int,
        y: Int,
        spanX: Int,
        spanY: Int,
        excludeItemId: String? = null
    ): Boolean {
        if (!isValidCell(x, y)) return false
        if (x + spanX > gridSize.columns || y + spanY > gridSize.rows) return false

        for (row in y until y + spanY) {
            for (col in x until x + spanX) {
                val occupant = grid[row][col]
                if (occupant != null && occupant != excludeItemId) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 전체 그리드를 초기화합니다.
     */
    internal fun clear() {
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                grid[row][col] = null
            }
        }
    }

    /**
     * 그리드의 복사본을 생성합니다.
     *
     * @return 새로운 OccupancyGrid 인스턴스
     */
    internal fun copy(): OccupancyGrid {
        val newGrid = OccupancyGrid(gridSize)
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                newGrid.grid[row][col] = grid[row][col]
            }
        }
        return newGrid
    }

    /**
     * 좌표가 유효한 범위 내에 있는지 확인합니다.
     */
    private fun isValidCell(x: Int, y: Int): Boolean {
        return x >= 0 && x < gridSize.columns && y >= 0 && y < gridSize.rows
    }

    /**
     * 디버깅용 문자열 표현
     */
    override fun toString(): String {
        return buildString {
            appendLine("OccupancyGrid(${gridSize.rows}x${gridSize.columns}):")
            for (row in grid.indices) {
                for (col in grid[row].indices) {
                    append(grid[row][col]?.take(3) ?: "___")
                    append(" ")
                }
                appendLine()
            }
        }
    }

    internal companion object {
        /**
         * 아이템 목록으로부터 OccupancyGrid를 생성합니다.
         *
         * @param gridSize 그리드 크기
         * @param items 배치할 아이템 목록
         * @return 새로운 OccupancyGrid 인스턴스
         * @throws IllegalStateException 아이템이 겹치는 경우
         */
        internal fun fromItems(gridSize: GridSize, items: List<GridItem>): OccupancyGrid {
            val occupancy = OccupancyGrid(gridSize)
            items.forEach { item ->
                occupancy.place(item)
            }
            return occupancy
        }
    }
}
