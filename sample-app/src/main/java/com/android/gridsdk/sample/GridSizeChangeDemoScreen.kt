package com.android.gridsdk.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.android.gridsdk.library.DynamicGridLayout
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine

@Composable
fun GridSizeChangeDemoScreen(modifier: Modifier = Modifier) {
    var gridSize by remember { mutableStateOf(GridSize.DEFAULT) }
    val items = remember { mutableStateListOf<GridItem>() }
    var nextId by remember { mutableStateOf(0) }
    var lastError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "N/M Runtime Change",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Adjust rows/columns. Shrinking removes out-of-bounds items.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rows: ${gridSize.rows}", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = gridSize.rows.toFloat(),
                        onValueChange = { newVal ->
                            val rows = newVal.toInt().coerceIn(2, 6)
                            gridSize = GridSize(rows = rows, columns = gridSize.columns)
                            items.removeAll { !it.isValidIn(gridSize) }
                        },
                        valueRange = 2f..6f,
                        steps = 4
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Columns: ${gridSize.columns}", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = gridSize.columns.toFloat(),
                        onValueChange = { newVal ->
                            val cols = newVal.toInt().coerceIn(2, 6)
                            gridSize = GridSize(rows = gridSize.rows, columns = cols)
                            items.removeAll { !it.isValidIn(gridSize) }
                        },
                        valueRange = 2f..6f,
                        steps = 4
                    )
                }
            }
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val item = GridItem(
                            id = "item_${nextId++}",
                            x = 0,
                            y = 0,
                            spanX = 1,
                            spanY = 1
                        )
                        val result = GridEngine.process(
                            EngineRequest.Add(item, items.toList(), gridSize)
                        )
                        when (result) {
                            is EngineResult.Success -> {
                                val newItems = result.applyTo(items.toList())
                                items.clear()
                                items.addAll(newItems)
                                lastError = null
                            }
                            is EngineResult.Failure -> lastError = result.error.toString()
                        }
                    }
                ) {
                    Text("Add")
                }
                lastError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            DynamicGridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = { newItems ->
                    items.clear()
                    items.addAll(newItems)
                },
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(item.id, style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${item.spanX}x${item.spanY}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = { items.removeAll { it.id == item.id } },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text("Del")
                            }
                        }
                    }
                }
            )
        }
    }
}
