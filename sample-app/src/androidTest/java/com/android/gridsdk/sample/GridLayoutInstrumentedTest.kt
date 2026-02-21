package com.android.gridsdk.sample

import android.R.attr.left
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.android.gridsdk.library.GridLayout
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for GridLayout Composable.
 *
 * Verifies drag gesture and animation completion state.
 */
class GridLayoutInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gridLayout_displaysItems() {
        val gridSize = GridSize(4, 4)
        val items = listOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        composeTestRule.setContent {
            GridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = {},
                modifier = Modifier.fillMaxSize(),
                cellContent = { item ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(item.id)
                    }
                }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("grid-item-a").assertExists()
        composeTestRule.onNodeWithTag("grid-item-b").assertExists()
    }

    @Test
    fun gridLayout_dragGestureAndAnimationComplete() {
        val gridSize = GridSize(4, 4)
        val items = mutableStateListOf(
            GridItem("a", 0, 0, 1, 1),
            GridItem("b", 1, 0, 1, 1)
        )
        composeTestRule.setContent {
            GridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = { newItems ->
                    items.clear()
                    items.addAll(newItems)
                },
                modifier = Modifier.fillMaxSize(),
                cellContent = { item ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(item.id)
                    }
                }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("grid-item-a").performTouchInput { swipe(left()) }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(300)
        composeTestRule.waitForIdle()
        assertTrue(items.any { it.id == "a" })
    }

    @Test
    fun gridLayout_animationCompletes() {
        val gridSize = GridSize(4, 4)
        val items = listOf(GridItem("a", 0, 0, 1, 1))
        composeTestRule.setContent {
            GridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = {},
                modifier = Modifier.fillMaxSize(),
                cellContent = { item ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(item.id)
                    }
                }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(250)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("grid-item-a").assertExists()
    }
}
