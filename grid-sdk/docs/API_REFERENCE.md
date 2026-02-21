# GridSdk API 레퍼런스

공개 API의 상세 스펙입니다. [PUBLIC_API.md](PUBLIC_API.md)와 함께 참고하세요.

---

## 1. GridLayout

**패키지**: `com.android.gridsdk.library`

N×M 고정 그리드 레이아웃 Composable. 드래그 이동, 롱프레스+드래그 리사이즈를 지원합니다.

```kotlin
@Composable
fun GridLayout(
    gridSize: GridSize,
    items: List<GridItem>,
    onItemsChange: (List<GridItem>) -> Unit,
    modifier: Modifier = Modifier,
    onFailure: ((GridError) -> Unit)? = null,
    cellContent: @Composable (GridItem) -> Unit = {}
)
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `gridSize` | `GridSize` | O | 그리드 크기 (행×열) |
| `items` | `List<GridItem>` | O | 현재 아이템 목록 |
| `onItemsChange` | `(List<GridItem>) -> Unit` | O | 아이템 변경 시 콜백 (드래그/리사이즈 결과 반영) |
| `modifier` | `Modifier` | - | 기본 `Modifier` |
| `onFailure` | `((GridError) -> Unit)?` | - | 엔진 실패 시 콜백 (null이면 무시) |
| `cellContent` | `@Composable (GridItem) -> Unit` | - | 각 아이템 렌더링 슬롯 |

---

## 2. GridEngine

**패키지**: `com.android.gridsdk.library.model.engine`

배치 엔진 진입점. `process()`와 `evaluateRollback()`를 제공합니다.

### process

```kotlin
fun process(request: EngineRequest): EngineResult
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `request` | `EngineRequest` | Add, Move, Resize 중 하나 |

| 반환 | 설명 |
|------|------|
| `EngineResult.Success` | 성공 시 `targetItem`, `relocatedItems` 포함 |
| `EngineResult.Failure` | 실패 시 `error` 포함 |

### evaluateRollback

```kotlin
fun evaluateRollback(
    currentItems: List<GridItem>,
    relocatedWithOriginals: Map<String, GridItem>,
    gridSize: GridSize
): List<GridItem>
```

드래그 중 재배치된 아이템 중 원위치 복귀 가능한 항목을 원위치로 되돌린 목록을 반환합니다. `GridLayout` 내부에서 자동 호출됩니다.

---

## 3. EngineRequest

**패키지**: `com.android.gridsdk.library.model.engine`

### Move

```kotlin
data class Move(
    val itemId: String,
    val targetX: Int,
    val targetY: Int,
    override val items: List<GridItem>,
    override val gridSize: GridSize
) : EngineRequest()
```

헬퍼: `EngineRequest.move(itemId, targetX, targetY, items, gridSize)`

### Resize

```kotlin
data class Resize(
    val itemId: String,
    val targetSpanX: Int,
    val targetSpanY: Int,
    override val items: List<GridItem>,
    override val gridSize: GridSize
) : EngineRequest()
```

헬퍼: `EngineRequest.resize(itemId, targetSpanX, targetSpanY, items, gridSize)`

### Add

```kotlin
data class Add(
    val item: GridItem,
    override val items: List<GridItem>,
    override val gridSize: GridSize
) : EngineRequest()
```

헬퍼: `EngineRequest.add(item, items, gridSize)`. Add 시 `item`의 `x`, `y`는 무시됩니다.

---

## 4. EngineResult

**패키지**: `com.android.gridsdk.library.model.engine`

### Success

```kotlin
data class Success(
    val targetItem: GridItem,
    val relocatedItems: List<GridItem>
) : EngineResult()
```

- `applyTo(originalItems: List<GridItem>): List<GridItem>` — 전체 목록에 결과 반영

### Failure

```kotlin
data class Failure(
    val error: GridError
) : EngineResult()
```

---

## 5. GridItem

**패키지**: `com.android.gridsdk.library.model`

```kotlin
data class GridItem(
    val id: String,
    val x: Int,
    val y: Int,
    val spanX: Int,
    val spanY: Int
)
```

| 프로퍼티 | 설명 |
|----------|------|
| `id` | 고유 식별자 |
| `x`, `y` | 시작 열/행 (0-based) |
| `spanX`, `spanY` | 차지하는 열/행 개수 (≥1) |

| 메서드 | 설명 |
|--------|------|
| `endX`, `endY` | 끝 좌표 (exclusive) |
| `area` | 셀 개수 |
| `isValidIn(gridSize)` | 그리드 내 유효 여부 |
| `occupiesCell(cellX, cellY)` | 셀 점유 여부 |
| `overlapsWith(other)` | 다른 아이템과 겹침 여부 |
| `moveTo(newX, newY)` | 위치 변경한 새 인스턴스 |
| `resize(newSpanX, newSpanY)` | span 변경한 새 인스턴스 |

헬퍼: `GridItem.single(id, x, y)` — 1×1 아이템 생성

---

## 6. GridSize

**패키지**: `com.android.gridsdk.library.model`

```kotlin
data class GridSize(
    val rows: Int,
    val columns: Int
)
```

| 상수 | 값 |
|------|-----|
| `GridSize.DEFAULT` | 4×4 |
| `GridSize.LAUNCHER_STANDARD` | 5×4 |

| 메서드 | 설명 |
|--------|------|
| `totalCells` | 전체 셀 개수 |
| `isValidPosition(row, column)` | 좌표 유효 여부 |
| `isWithinBounds(row, column, rowSpan, columnSpan)` | 범위 내 여부 |

---

## 7. GridError

**패키지**: `com.android.gridsdk.library.model`

| 타입 | 설명 |
|------|------|
| `OutOfBounds` | 경계 초과 (`itemId`, `position`, `gridSize`) |
| `NoFeasibleLayout` | 유효 배치 없음 (`itemId`, `reason`, `conflictingItems`) |
| `ItemOverlap` | 아이템 겹침 |
| `ItemNotFound` | 아이템 미존재 |
| `InvalidItem` | 유효하지 않은 아이템 (span 등) |
| `GridFull` | 그리드 가득 참 |
| `DuplicateItemId` | 중복 ID |
| `Position` | 위치 정보 (`x`, `y`, `spanX`, `spanY`) |

---

## 8. GridSdkInfo

**패키지**: `com.android.gridsdk.library`

라이브러리 버전/이름 상수 (`VERSION`, `LIBRARY_NAME`).
