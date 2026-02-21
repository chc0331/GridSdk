package com.android.gridsdk.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.GridLayout
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine

@Composable
fun EdgeCaseDemoScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        NoFeasibleLayoutSection(modifier = Modifier.fillMaxWidth())
        RollbackSection(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoFeasibleLayoutSection(modifier: Modifier = Modifier) {
    val gridSize = GridSize(rows = 4, columns = 4)
    val items = remember {
        mutableStateListOf(
            GridItem("a", 0, 0, 2, 2),
            GridItem("b", 2, 0, 2, 2),
            GridItem("c", 0, 2, 2, 2),
            GridItem("d", 2, 2, 2, 2)
        )
    }
    var lastError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "NoFeasibleLayout: Move 'a' to center",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "4x4 grid with four 2x2 items. Moving 'a' to (1,1) has no valid placement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                val result = GridEngine.process(
                    EngineRequest.Move(
                        itemId = "a",
                        targetX = 1,
                        targetY = 1,
                        items = items.toList(),
                        gridSize = gridSize
                    )
                )
                when (result) {
                    is EngineResult.Success -> lastError = null
                    is EngineResult.Failure -> lastError = result.error.toString()
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Move 'a' to center (1,1)")
        }
        lastError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            GridLayout(
                    gridSize = gridSize,
                    items = items,
                    onItemsChange = { items.clear(); items.addAll(it) },
                    modifier = Modifier.fillMaxSize(),
                    onFailure = { lastError = it.toString() },
                    cellContent = { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.id, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
        }
    }
}

@Composable
private fun RollbackSection(modifier: Modifier = Modifier) {
    val gridSize = GridSize(rows = 3, columns = 4)
    val items = remember {
        mutableStateListOf(
            GridItem("a", 0, 0, 2, 1),
            GridItem("b", 2, 0, 1, 1)
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Rollback: Drag back to restore",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Drag 'a' right → 'b' relocates. Drag 'a' back left → 'b' rolls back to original.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp)
        ) {
            GridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = { items.clear(); items.addAll(it) },
                modifier = Modifier.fillMaxSize(),
                cellContent = { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.id, style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    }
}
