package com.android.gridsdk.library.internal.state

import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.engine.EngineResult
import org.junit.Assert.*
import org.junit.Test

class StateSnapshotTest {

    @Test
    fun `apply returns new items`() {
        val previous = listOf(GridItem("a", 0, 0, 1, 1))
        val newItems = listOf(GridItem("a", 1, 1, 1, 1))

        val snapshot = StateSnapshot(previousItems = previous, newItems = newItems)

        assertEquals(newItems, snapshot.apply())
    }

    @Test
    fun `rollback returns previous items`() {
        val previous = listOf(GridItem("a", 0, 0, 1, 1))
        val newItems = listOf(GridItem("a", 1, 1, 1, 1))

        val snapshot = StateSnapshot(previousItems = previous, newItems = newItems)

        assertEquals(previous, snapshot.rollback())
    }

    @Test
    fun `fromSuccess creates snapshot matching EngineResult applyTo`() {
        val original = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        val success = EngineResult.Success(
            targetItem = GridItem("a", 1, 0, 1, 1),
            relocatedItems = listOf(GridItem("b", 0, 0, 1, 1))
        )

        val snapshot = StateSnapshot.fromSuccess(original, success)

        assertEquals(original, snapshot.rollback())
        assertEquals(success.applyTo(original), snapshot.apply())
    }
}
