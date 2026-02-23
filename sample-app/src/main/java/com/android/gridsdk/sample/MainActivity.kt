package com.android.gridsdk.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.gridsdk.library.DynamicGridLayout
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.GridEngine
import com.android.gridsdk.sample.ui.theme.GridSdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private enum class DemoTab { Basic, EdgeCase, GridSize }

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(DemoTab.Basic) }
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == DemoTab.Basic,
                onClick = { selectedTab = DemoTab.Basic },
                text = { Text("Basic") }
            )
            Tab(
                selected = selectedTab == DemoTab.EdgeCase,
                onClick = { selectedTab = DemoTab.EdgeCase },
                text = { Text("Edge Case") }
            )
            Tab(
                selected = selectedTab == DemoTab.GridSize,
                onClick = { selectedTab = DemoTab.GridSize },
                text = { Text("N/M Change") }
            )
        }
        when (selectedTab) {
            DemoTab.Basic -> GridDemoScreen(modifier = Modifier.fillMaxSize())
            DemoTab.EdgeCase -> EdgeCaseDemoScreen(modifier = Modifier.fillMaxSize())
            DemoTab.GridSize -> GridSizeChangeDemoScreen(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun GridDemoScreen(modifier: Modifier = Modifier) {
    val gridSize = GridSize.DEFAULT
    val items = remember { mutableStateListOf<GridItem>() }
    var nextId by remember { mutableStateOf(0) }
    var lastError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Drag to move. Long press to show resize handle, then drag handle to resize.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val item = GridItem(
                            id = "item_${nextId++}",
                            x = 0,
                            y = 0,
                            spanX = 2,
                            spanY = 2
                        )
                        val result = GridEngine.process(
                            EngineRequest.Add(item, items.toList(), gridSize)
                        )
                        when (result) {
                            is com.android.gridsdk.library.model.engine.EngineResult.Success -> {
                                val newItems = result.applyTo(items.toList())
                                items.clear()
                                items.addAll(newItems)
                                lastError = null
                            }
                            is com.android.gridsdk.library.model.engine.EngineResult.Failure -> {
                                lastError = result.error.toString()
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
                Button(
                    onClick = {
                        lastError = null
                        while (true) {
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
                                is com.android.gridsdk.library.model.engine.EngineResult.Success -> {
                                    val newItems = result.applyTo(items.toList())
                                    items.clear()
                                    items.addAll(newItems)
                                }
                                is com.android.gridsdk.library.model.engine.EngineResult.Failure -> {
                                    lastError = result.error.toString()
                                    break
                                }
                            }
                        }
                    }
                ) {
                    Text("Add until full")
                }
                lastError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
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
                onFailure = { error ->
                    lastError = error.toString()
                },
                cellContent = { item ->
                    GridItemContent(
                        item = item,
                        onRemove = {
                            items.removeAll { it.id == item.id }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun GridItemContent(
    item: GridItem,
    onRemove: () -> Unit
) {
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
            Text(
                text = item.id,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "${item.spanX}x${item.spanY}",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onRemove,
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Del")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridDemoPreview() {
    GridSdkTheme {
        val gridSize = GridSize.DEFAULT
        val items = remember {
            mutableStateListOf(
                GridItem.single("a", 0, 0),
                GridItem.single("b", 1, 0),
                GridItem.single("c", 0, 1)
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            DynamicGridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = { items.clear(); items.addAll(it) },
                cellContent = { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.id)
                    }
                }
            )
        }
    }
}
