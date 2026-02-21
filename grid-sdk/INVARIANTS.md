# 그리드 불변조건 (Grid Invariants)

배치 엔진 및 그리드 상태가 **항상** 유지해야 하는 불변조건입니다.  
이 조건을 위반하는 상태는 유효하지 않으며, 엔진은 위반 상태를 생성·반환하지 않습니다.

---

## 1. 경계 초과 금지 (No Out-of-Bounds)

### 정의

- 모든 아이템은 그리드 경계를 벗어나면 안 됩니다.
- 그리드는 고정 `N x M` 구조이며, `N`(행), `M`(열)은 설정값으로 정해집니다.

### 수식

- 그리드 크기: `GridSize(rows = N, columns = M)`
- 아이템: `GridItem(id, x, y, spanX, spanY)`
- **불변조건**: 모든 아이템에 대해
  - `0 ≤ x`
  - `0 ≤ y`
  - `x + spanX ≤ M`
  - `y + spanY ≤ N`

### 코드에서의 검증

- **모델**: `GridSize.isWithinBounds(y, x, spanY, spanX)`, `GridItem.isValidIn(gridSize)`
- **유틸**: `ValidationUtils.isWithinBounds(item, gridSize)`, `ValidationUtils.allWithinBounds(items, gridSize)`
- **점유 상태**: `OccupancyGrid.place(item)` 호출 전 경계 검사 수행, 위반 시 예외

### 위반 시 동작

- **엔진**: 해당 배치/이동/리사이즈를 적용하지 않고 `EngineResult.Failure(GridError.OutOfBounds(...))` 반환
- **실패 시**: 기존 아이템 목록 및 상태 변경 없음

---

## 2. 중복 점유 금지 (No Overlapping Items)

### 정의

- 아이템 간 셀 중복 점유는 허용하지 않습니다.
- 한 셀은 최대 하나의 아이템만 점유할 수 있습니다.

### 수식

- 서로 다른 두 아이템 `A`, `B`에 대해  
  `A`가 차지하는 영역 `[A.x, A.x+A.spanX) × [A.y, A.y+A.spanY)` 와  
  `B`가 차지하는 영역이 **겹치면 안 됩니다**.
- 즉, `A.overlapsWith(B) == false` (단, `A.id != B.id`)

### 코드에서의 검증

- **모델**: `GridItem.overlapsWith(other)`, `GridItem.occupiesCell(cellX, cellY)`
- **유틸**: `ValidationUtils.hasOverlap`, `ValidationUtils.findOverlappingItems`, `ValidationUtils.hasAnyOverlap`
- **점유 상태**: `OccupancyGrid`는 셀당 하나의 아이템 ID만 저장; `place(item)` 시 해당 영역이 비어 있지 않으면 예외

### 위반 시 동작

- **엔진**: 해당 배치를 유효 후보로 채택하지 않음. 유효 후보가 없으면 `EngineResult.Failure(GridError.ItemOverlap(...))` 또는 `GridError.NoFeasibleLayout(...)` 반환
- **실패 시**: 기존 상태 보존

---

## 3. 유효 후보의 정의 (PRD 연계)

- **유효 후보**: 위 두 불변조건(경계 내 + 중복 없음)을 모두 만족하는 배치 상태만 유효 후보로 간주합니다.
- 경계를 벗어나거나, 어떤 셀이라도 두 아이템에 의해 점유되는 배치는 유효 후보가 아니며, 엔진은 이를 선택·반환하지 않습니다.

---

## 4. 요약 표

| 불변조건       | 내용                 | 위반 시 엔진 동작                          |
|----------------|----------------------|--------------------------------------------|
| 경계 초과 금지 | 모든 아이템이 N×M 내 | `Failure(OutOfBounds)` 등, 기존 상태 유지  |
| 중복 점유 금지 | 셀당 최대 1개 아이템 | 유효 후보 미선택 / `Failure`, 기존 상태 유지 |

---

## 5. 관련 타입

- **모델**: `GridSize`, `GridItem` — 경계·겹침 검사 메서드 제공
- **에러**: `GridError.OutOfBounds`, `GridError.ItemOverlap`, `GridError.NoFeasibleLayout`
- **점유**: `OccupancyGrid` — 두 불변조건을 만족하는 상태만 유지하도록 구현

이 문서는 PRD의 「공통 제약」「실패 조건」을 코드 및 동작과 맞춘 불변조건 명세입니다.
