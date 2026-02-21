package com.android.gridsdk.library.model.engine

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.*
import org.junit.Test

class EngineRequestsTest {

    private val gridSize = GridSize(rows = 5, columns = 4)
    private val items = listOf(
        GridItem("item1", x = 0, y = 0, spanX = 1, spanY = 1),
        GridItem("item2", x = 1, y = 0, spanX = 2, spanY = 1)
    )

    @Test
    fun `MoveRequest contains all required fields`() {
        val request = MoveRequest(
            itemId = "item1",
            targetX = 2,
            targetY = 1,
            items = items,
            gridSize = gridSize
        )

        assertEquals("item1", request.itemId)
        assertEquals(2, request.targetX)
        assertEquals(1, request.targetY)
        assertEquals(2, request.items.size)
        assertEquals(gridSize, request.gridSize)
    }

    @Test
    fun `ResizeRequest contains all required fields`() {
        val request = ResizeRequest(
            itemId = "item1",
            targetSpanX = 2,
            targetSpanY = 2,
            items = items,
            gridSize = gridSize
        )

        assertEquals("item1", request.itemId)
        assertEquals(2, request.targetSpanX)
        assertEquals(2, request.targetSpanY)
        assertEquals(2, request.items.size)
        assertEquals(gridSize, request.gridSize)
    }

    @Test
    fun `AddRequest contains all required fields`() {
        val newItem = GridItem("item3", x = 0, y = 0, spanX = 2, spanY = 1)
        val request = AddRequest(
            item = newItem,
            items = items,
            gridSize = gridSize
        )

        assertEquals(newItem, request.item)
        assertEquals("item3", request.item.id)
        assertEquals(2, request.item.spanX)
        assertEquals(1, request.item.spanY)
        assertEquals(2, request.items.size)
        assertEquals(gridSize, request.gridSize)
    }

    @Test
    fun `MoveRequest is data class with equality`() {
        val req1 = MoveRequest("item1", 1, 1, items, gridSize)
        val req2 = MoveRequest("item1", 1, 1, items, gridSize)
        val req3 = MoveRequest("item1", 2, 2, items, gridSize)

        assertEquals(req1, req2)
        assertNotEquals(req1, req3)
    }

    @Test
    fun `ResizeRequest is data class with equality`() {
        val req1 = ResizeRequest("item1", 2, 2, items, gridSize)
        val req2 = ResizeRequest("item1", 2, 2, items, gridSize)
        val req3 = ResizeRequest("item1", 3, 3, items, gridSize)

        assertEquals(req1, req2)
        assertNotEquals(req1, req3)
    }

    @Test
    fun `AddRequest is data class with equality`() {
        val item = GridItem("item3", x = 0, y = 0, spanX = 1, spanY = 1)
        val req1 = AddRequest(item, items, gridSize)
        val req2 = AddRequest(item, items, gridSize)

        assertEquals(req1, req2)
    }

    @Test
    fun `EngineRequest sealed class - common fields accessible`() {
        val moveReq: EngineRequest = EngineRequest.Move("item1", 1, 1, items, gridSize)
        val resizeReq: EngineRequest = EngineRequest.Resize("item1", 2, 2, items, gridSize)
        val addReq: EngineRequest = EngineRequest.Add(
            GridItem("item3", 0, 0, 1, 1),
            items,
            gridSize
        )

        assertEquals(items, moveReq.items)
        assertEquals(gridSize, moveReq.gridSize)
        assertEquals(items, resizeReq.items)
        assertEquals(items, addReq.items)
    }

    @Test
    fun `EngineRequest sealed class - exhaustive when`() {
        val requests: List<EngineRequest> = listOf(
            EngineRequest.Move("item1", 1, 1, items, gridSize),
            EngineRequest.Resize("item1", 2, 2, items, gridSize),
            EngineRequest.Add(GridItem("item3", 0, 0, 1, 1), items, gridSize)
        )

        requests.forEach { req ->
            when (req) {
                is EngineRequest.Move -> assertEquals("item1", req.itemId)
                is EngineRequest.Resize -> assertEquals("item1", req.itemId)
                is EngineRequest.Add -> assertEquals("item3", req.item.id)
            }
        }
    }

    @Test
    fun `companion object factory methods work`() {
        val moveReq = EngineRequest.move("item1", 2, 3, items, gridSize)
        val resizeReq = EngineRequest.resize("item1", 2, 3, items, gridSize)
        val item = GridItem("item3", 0, 0, 1, 1)
        val addReq = EngineRequest.add(item, items, gridSize)

        assertTrue(moveReq is EngineRequest.Move)
        assertEquals(2, moveReq.targetX)
        assertEquals(3, moveReq.targetY)

        assertTrue(resizeReq is EngineRequest.Resize)
        assertEquals(2, resizeReq.targetSpanX)
        assertEquals(3, resizeReq.targetSpanY)

        assertTrue(addReq is EngineRequest.Add)
        assertEquals(item, addReq.item)
    }
}
