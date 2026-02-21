# GridSdk API 사용 예제

필요한 import:

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import com.android.gridsdk.library.GridLayout
import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine
```

---

## 1. 최소 예제 (Minimal)

가장 단순한 그리드 레이아웃 사용 예제입니다.

```kotlin
@Composable
fun MinimalGridDemo() {
    val gridSize = GridSize.DEFAULT  // 4x4
    val items = remember { mutableStateListOf<GridItem>() }

    GridLayout(
        gridSize = gridSize,
        items = items,
        onItemsChange = { newItems ->
            items.clear()
            items.addAll(newItems)
        },
        cellContent = { item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(item.id)
            }
        }
    )
}
```

### 아이템 추가 (Add)

```kotlin
val item = GridItem(id = "item_1", x = 0, y = 0, spanX = 1, spanY = 1)
val result = GridEngine.process(
    EngineRequest.Add(item, items.toList(), gridSize)
)
when (result) {
    is EngineResult.Success -> {
        val newItems = result.applyTo(items.toList())
        items.clear()
        items.addAll(newItems)
    }
    is EngineResult.Failure -> {
        // result.error 처리 (예: Toast, Snackbar)
    }
}
```

> **참고**: Add 요청 시 `item`의 `x`, `y`는 무시됩니다. 엔진이 상단-좌측 첫 빈 공간에 자동 배치합니다.

---

## 2. 고급 예제 (Advanced)

### 2.1 Add + Delete + 실패 처리

```kotlin
@Composable
fun AdvancedGridDemo() {
    val gridSize = GridSize(rows = 5, columns = 4)
    val items = remember { mutableStateListOf<GridItem>() }
    var nextId by remember { mutableStateOf(0) }
    var lastError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val item = GridItem(
                        id = "item_${nextId++}",
                        x = 0, y = 0,  // Add 시 무시됨
                        spanX = 1, spanY = 1
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
                        is EngineResult.Failure -> {
                            lastError = result.error.toString()
                            // GridFull, DuplicateItemId 등 처리
                        }
                    }
                }
            ) { Text("Add") }
            lastError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        Box(modifier = Modifier.weight(1f)) {
            GridLayout(
                gridSize = gridSize,
                items = items,
                onItemsChange = { items.clear(); items.addAll(it) },
                onFailure = { error -> lastError = error.toString() },
                cellContent = { item ->
                    GridItemWithDelete(
                        item = item,
                        onDelete = { items.removeAll { it.id == item.id } }
                    )
                }
            )
        }
    }
}
```

### 2.2 Engine 헬퍼 사용

```kotlin
// EngineRequest companion object 헬퍼 사용
val moveRequest = EngineRequest.move(
    itemId = "item_1",
    targetX = 2,
    targetY = 1,
    items = items,
    gridSize = gridSize
)
val result = GridEngine.process(moveRequest)

// Add 헬퍼
val addRequest = EngineRequest.add(
    item = GridItem.single("new_item", 0, 0),
    items = items,
    gridSize = gridSize
)
```

### 2.3 GridItem.single 활용

```kotlin
// 1x1 아이템 빠른 생성
val item = GridItem.single(id = "a", x = 0, y = 0)
// 동일: GridItem("a", 0, 0, 1, 1)
```

### 2.4 GridSize 상수 활용

```kotlin
val gridSize = GridSize.DEFAULT           // 4x4
val gridSize = GridSize.LAUNCHER_STANDARD // 5x4
val gridSize = GridSize(rows = 6, columns = 5)  // 커스텀
```

### 2.5 에러 타입별 처리

```kotlin
onFailure = { error ->
    when (error) {
        is GridError.GridFull -> showMessage("그리드가 가득 찼습니다.")
        is GridError.DuplicateItemId -> showMessage("중복 ID: ${error.itemId}")
        is GridError.OutOfBounds -> showMessage("경계 초과")
        is GridError.NoFeasibleLayout -> showMessage("배치 불가: ${error.reason}")
        else -> showMessage(error.toString())
    }
}
```

### 2.6 프로그래매틱 Move/Resize (GridLayout 외부)

드래그/리사이즈는 GridLayout이 자동 처리합니다.  
코드로 직접 이동/리사이즈하려면:

```kotlin
// Move
val moveResult = GridEngine.process(
    EngineRequest.Move(
        itemId = "item_1",
        targetX = 1, targetY = 2,
        items = items,
        gridSize = gridSize
    )
)

// Resize
val resizeResult = GridEngine.process(
    EngineRequest.Resize(
        itemId = "item_1",
        targetSpanX = 2, targetSpanY = 2,
        items = items,
        gridSize = gridSize
    )
)
```

### 2.7 evaluateRollback (드래그 중 롤백)

GridLayout 내부에서 자동 호출됩니다.  
**직접 호출**이 필요한 경우 (커스텀 드래그 구현 시):

```kotlin
val rolledBack = GridEngine.evaluateRollback(
    currentItems = currentItems,
    relocatedWithOriginals = mapOf("item_2" to originalItem2),
    gridSize = gridSize
)
// rolledBack: 복귀 적용된 아이템 목록
```

---

## 3. 전체 흐름 요약

| 동작 | 사용자 입력 | SDK 처리 |
|------|-------------|----------|
| Add | 버튼 클릭 → `GridEngine.process(Add)` | 상단-좌측 첫 빈 공간 배치 |
| Delete | 아이템 내 삭제 버튼 → `items.remove()` | 상태에서 제거 |
| Move | 드래그 | `GridLayout` 내부에서 Move 요청 → 재배치 |
| Resize | 롱프레스 후 드래그 | `GridLayout` 내부에서 Resize 요청 → 재배치 |
| Rollback | 드래그 중 포인터 이동 | `GridLayout` 내부에서 `evaluateRollback` 자동 호출 |
