package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize

/**
 * 배치 엔진 입력 DTO
 *
 * Sealed class로 정의하여 요청 타입을 타입 안전하게 표현합니다.
 * PRD 알고리즘 스펙에 따른 입력 형식:
 * - 현재 아이템 목록
 * - 대상 아이템
 * - 목표 좌표 또는 목표 Span
 * - N, M (그리드 크기)
 */
public sealed class EngineRequest {

    /** 현재 그리드의 아이템 목록 */
    public abstract val items: List<GridItem>

    /** 그리드 크기 (N x M) */
    public abstract val gridSize: GridSize

    /**
     * 아이템 이동 요청
     *
     * @property itemId 이동할 아이템 ID
     * @property targetX 목표 x 좌표 (열)
     * @property targetY 목표 y 좌표 (행)
     */
    public data class Move(
        val itemId: String,
        val targetX: Int,
        val targetY: Int,
        override val items: List<GridItem>,
        override val gridSize: GridSize
    ) : EngineRequest()

    /**
     * 아이템 리사이즈 요청
     *
     * @property itemId 리사이즈할 아이템 ID
     * @property targetSpanX 목표 spanX
     * @property targetSpanY 목표 spanY
     */
    public data class Resize(
        val itemId: String,
        val targetSpanX: Int,
        val targetSpanY: Int,
        override val items: List<GridItem>,
        override val gridSize: GridSize
    ) : EngineRequest()

    /**
     * 아이템 추가 요청
     *
     * @property item 추가할 아이템 (id, spanX, spanY 포함, x/y는 엔진이 배치)
     */
    public data class Add(
        val item: GridItem,
        override val items: List<GridItem>,
        override val gridSize: GridSize
    ) : EngineRequest()

    public companion object {
        /** [Move] 요청 생성 헬퍼 */
        public fun move(
            itemId: String,
            targetX: Int,
            targetY: Int,
            items: List<GridItem>,
            gridSize: GridSize
        ): Move = Move(itemId, targetX, targetY, items, gridSize)

        /** [Resize] 요청 생성 헬퍼 */
        public fun resize(
            itemId: String,
            targetSpanX: Int,
            targetSpanY: Int,
            items: List<GridItem>,
            gridSize: GridSize
        ): Resize = Resize(itemId, targetSpanX, targetSpanY, items, gridSize)

        /** [Add] 요청 생성 헬퍼 */
        public fun add(
            item: GridItem,
            items: List<GridItem>,
            gridSize: GridSize
        ): Add = Add(item, items, gridSize)
    }
}

/** [EngineRequest.Move] 타입 별칭 */
public typealias MoveRequest = EngineRequest.Move

/** [EngineRequest.Resize] 타입 별칭 */
public typealias ResizeRequest = EngineRequest.Resize

/** [EngineRequest.Add] 타입 별칭 */
public typealias AddRequest = EngineRequest.Add
