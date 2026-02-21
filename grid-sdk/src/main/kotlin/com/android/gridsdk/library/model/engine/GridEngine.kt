package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.internal.engine.PlacementExplorer
import com.android.gridsdk.library.internal.engine.RollbackEvaluator

/**
 * 그리드 배치 엔진의 공개 진입점
 *
 * [EngineRequest]를 받아 [EngineResult]를 반환합니다.
 * Add, Move, Resize 요청을 처리합니다.
 */
public object GridEngine {

    /**
     * 엔진 요청을 처리하고 결과를 반환합니다.
     *
     * @param request 처리할 요청 (Add, Move, Resize)
     * @return 성공 시 [EngineResult.Success], 실패 시 [EngineResult.Failure]
     */
    public fun process(request: EngineRequest): EngineResult {
        return when (request) {
            is EngineRequest.Add -> processAdd(request)
            is EngineRequest.Move -> processMove(request)
            is EngineRequest.Resize -> processResize(request)
        }
    }

    private fun processAdd(request: EngineRequest.Add): EngineResult {
        val item = request.item
        val items = request.items
        val gridSize = request.gridSize

        // 중복 ID 체크
        if (items.any { it.id == item.id }) {
            return EngineResult.failure(GridError.DuplicateItemId(item.id))
        }

        // span 유효성 검사 (GridItem 생성자에서 검사되지만, 사전 검증)
        if (item.spanX < 1 || item.spanY < 1) {
            return EngineResult.failure(
                GridError.InvalidItem(item.id, "spanX and spanY must be greater than 0")
            )
        }

        val placedItem = PlacementExplorer.exploreAddPosition(items, item, gridSize)
            ?: return EngineResult.failure(GridError.GridFull(gridSize))

        return EngineResult.success(placedItem, relocatedItems = emptyList())
    }

    private fun processMove(request: EngineRequest.Move): EngineResult {
        val targetItem = request.items.find { it.id == request.itemId }
            ?: return EngineResult.failure(GridError.ItemNotFound(request.itemId))
        val movedItem = targetItem.moveTo(request.targetX, request.targetY)
        if (!movedItem.isValidIn(request.gridSize)) {
            return EngineResult.failure(
                GridError.OutOfBounds(
                    itemId = request.itemId,
                    position = GridError.Position(
                        request.targetX,
                        request.targetY,
                        movedItem.spanX,
                        movedItem.spanY
                    ),
                    gridSize = request.gridSize
                )
            )
        }
        val candidate = PlacementExplorer.exploreBestCandidate(
            request.items,
            movedItem,
            request.gridSize
        )
        return if (candidate != null) {
            val finalTarget = candidate.find { it.id == request.itemId }!!
            val relocated = candidate.filter { c ->
                c.id != request.itemId && run {
                    val orig = request.items.find { it.id == c.id }
                    orig != null && (c.x != orig.x || c.y != orig.y)
                }
            }
            EngineResult.success(finalTarget, relocated)
        } else {
            EngineResult.failure(
                GridError.NoFeasibleLayout(
                    itemId = request.itemId,
                    reason = "No valid placement for move to (${request.targetX}, ${request.targetY})"
                )
            )
        }
    }

    private fun processResize(request: EngineRequest.Resize): EngineResult {
        val targetItem = request.items.find { it.id == request.itemId }
            ?: return EngineResult.failure(GridError.ItemNotFound(request.itemId))

        // span 최소/최대 제약 검증
        if (request.targetSpanX < 1 || request.targetSpanY < 1) {
            return EngineResult.failure(
                GridError.InvalidItem(
                    request.itemId,
                    "spanX and spanY must be greater than 0, got ${request.targetSpanX}x${request.targetSpanY}"
                )
            )
        }

        val resizedItem = targetItem.resize(request.targetSpanX, request.targetSpanY)
        if (!resizedItem.isValidIn(request.gridSize)) {
            return EngineResult.failure(
                GridError.OutOfBounds(
                    itemId = request.itemId,
                    position = GridError.Position(
                        targetItem.x,
                        targetItem.y,
                        request.targetSpanX,
                        request.targetSpanY
                    ),
                    gridSize = request.gridSize
                )
            )
        }

        val candidate = PlacementExplorer.exploreBestCandidate(
            request.items,
            resizedItem,
            request.gridSize
        )
        return if (candidate != null) {
            val finalTarget = candidate.find { it.id == request.itemId }!!
            val relocated = candidate.filter { c ->
                c.id != request.itemId && run {
                    val orig = request.items.find { it.id == c.id }
                    orig != null && (c.x != orig.x || c.y != orig.y)
                }
            }
            EngineResult.success(finalTarget, relocated)
        } else {
            EngineResult.failure(
                GridError.NoFeasibleLayout(
                    itemId = request.itemId,
                    reason = "No valid placement for resize to ${request.targetSpanX}x${request.targetSpanY}"
                )
            )
        }
    }

    /**
     * 드래그 중 재배치된 아이템 중 원위치 복귀 가능한 항목을 원위치로 되돌린 레이아웃을 반환합니다.
     *
     * PRD 롤백 규칙: "드래그 중 재배치되었던 아이템은, 본래 위치가 다시 유효해지면 원위치로 복귀"
     * 호출자는 Move/Resize 성공 후 이 메서드를 호출하여 롤백을 적용할 수 있습니다.
     *
     * @param currentItems 현재 아이템 레이아웃 (드래그/리사이즈 결과 적용 후)
     * @param relocatedWithOriginals 재배치된 아이템의 원래 위치 (itemId -> originalItem)
     * @param gridSize 그리드 크기
     * @return 복귀 적용된 아이템 목록 (복귀 불가 시 currentItems와 동일)
     */
    public fun evaluateRollback(
        currentItems: List<GridItem>,
        relocatedWithOriginals: Map<String, GridItem>,
        gridSize: GridSize
    ): List<GridItem> {
        return RollbackEvaluator.evaluateRollback(
            currentItems,
            relocatedWithOriginals,
            gridSize
        )
    }
}
