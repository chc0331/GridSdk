# PRD 핵심 규칙별 테스트 매트릭스

PRD의 핵심 규칙과 이를 검증하는 테스트의 매핑입니다.

---

## 1. 공통 제약

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 경계 초과 금지 | `GridEngineMoveTest` | `move to out of bounds returns OutOfBounds` | 기존 |
| 경계 초과 금지 | `GridEngineResizeTest` | `resize to out of bounds returns OutOfBounds` | 기존 |
| 경계 초과 금지 | `CandidateValidatorTest` | 경계 검증 테스트 | 기존 |
| 경계 초과 금지 | `ValidationUtilsTest` | `isWithinBounds`, `allWithinBounds` | 기존 |
| 중복 점유 금지 | `OccupancyGridTest` | `place` 충돌, `getConflictingItems` | 기존 |
| 중복 점유 금지 | `ValidationUtilsTest` | `hasOverlap`, `hasAnyOverlap` | 기존 |
| 중복 점유 금지 | `PlacementExplorerTest` | 유효 후보 검증 | 기존 |
| 중복 점유 금지 | `GridEnginePropertyTest` | 성공 시 `hasAnyOverlap == false` | 신규 |

---

## 2. Add 동작

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 상단-좌측(행 우선, 열 우선) 빈 공간 탐색 | `GridEngineAddTest` | `add to empty grid places item at 0,0` | 기존 |
| 상단-좌측 배치 | `GridEngineAddTest` | `add to partially occupied grid places at first empty top-left` | 기존 |
| Span 고려 첫 유효 위치 | `GridEngineAddTest` | `add with span considers first fitting slot` | 기존 |
| 가능한 위치 없으면 추가 거부 | `GridEngineAddTest` | `add to full grid returns GridFull failure` | 기존 |
| 중복 ID 거부 | `GridEngineAddTest` | `add with duplicate id returns DuplicateItemId failure` | 기존 |
| 실패 시 기존 상태 불변 | `GridEngineAddTest` | `add failure does not modify original items` | 기존 |

---

## 3. Move 동작

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 무충돌 이동 | `GridEngineMoveTest` | `move with no conflict succeeds` | 기존 |
| 충돌 아이템 재배치 | `GridEngineMoveTest` | `move with single conflict relocates one item` | 기존 |
| 다중 충돌 재배치 | `GridEngineMoveTest` | `move with multiple conflicts relocates minimally` | 기존 |
| OutOfBounds 실패 | `GridEngineMoveTest` | `move to out of bounds returns OutOfBounds` | 기존 |
| NoFeasibleLayout 실패 | `GridEngineMoveTest` | `move when no feasible layout returns NoFeasibleLayout` | 기존 |
| ItemNotFound 실패 | `GridEngineMoveTest` | `move with non-existent item returns ItemNotFound` | 기존 |
| 실패 시 상태 불변 | `GridEngineMoveTest` | `move failure does not modify original items` | 기존 |
| tie-break 상단-좌측 | `GridEngineMoveTest` | `move tie-break prefers top-left when multiple candidates have same score` | 기존 |

---

## 4. Resize 동작

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 무충돌 확장/축소 | `GridEngineResizeTest` | `resize expand with no conflict succeeds`, `resize shrink with no conflict succeeds` | 기존 |
| 충돌 아이템 재배치 | `GridEngineResizeTest` | `resize expand with conflict relocates conflicting item` | 기존 |
| OutOfBounds 실패 | `GridEngineResizeTest` | `resize to out of bounds returns OutOfBounds` | 기존 |
| InvalidItem 실패 | `GridEngineResizeTest` | `resize with invalid span returns InvalidItem` | 기존 |
| NoFeasibleLayout 실패 | `GridEngineResizeTest` | `resize when no feasible layout returns NoFeasibleLayout` | 기존 |
| ItemNotFound 실패 | `GridEngineResizeTest` | `resize with non-existent item returns ItemNotFound` | 기존 |
| 실패 시 상태 불변 | `GridEngineResizeTest` | `resize failure does not modify original items` | 기존 |
| 재배치 수 최소화 | `GridEngineResizeTest` | `resize result minimizes relocated item count` | 기존 |

---

## 5. 후보 우선순위

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 1. 재배치 수 최소 | `GridEngineMoveTest`, `GridEngineResizeTest` | `relocates minimally`, `relocates exactly one/two` | 기존 |
| 2. 맨해튼 거리 최소 | `CandidateScorerTest` | 거리 스코어 계산 | 기존 |
| 3. 상단-좌측 우선 | `GridEngineMoveTest` | `tie-break prefers top-left` | 기존 |
| 3. 상단-좌측 우선 | `CandidateScorerTest` | `topLeftScore` | 기존 |

---

## 6. 롤백 규칙

| PRD 규칙 | 관련 테스트 | 테스트 케이스 | 상태 |
|----------|-------------|---------------|------|
| 원위치 유효 시 복귀 | `GridEngineRollbackTest` | `rollback possible when dragged item moves away` | 기존 |
| 원위치 점유 시 비복귀 | `GridEngineRollbackTest` | `rollback not possible when dragged item still occupies` | 기존 |
| 다중 복귀 | `GridEngineRollbackTest` | `multiple rollbacks when both relocated items original positions become free` | 기존 |
| 부분 복귀 | `GridEngineRollbackTest` | `partial rollback when only one relocated item original position is free` | 기존 |
| 복귀 후 중복/경계 검증 | `GridEngineRollbackRegressionTest` | 복귀 결과 유효성 | 신규 |

---

## 7. 실패 조건 및 상태 불변

| PRD 규칙 | 관련 테스트 | 상태 |
|----------|-------------|------|
| 실패 시 기존 상태 보존 | `GridEngineStateInvariantTest` | 신규 |
| 실패 시 기존 상태 보존 | `GridEngineAddTest`, `GridEngineMoveTest`, `GridEngineResizeTest` | 기존 |

---

## 8. N/M 변경 회귀

| PRD 규칙 | 관련 테스트 | 상태 |
|----------|-------------|------|
| 다양한 GridSize에서 동작 | `GridEngineGridSizeRegressionTest` | 신규 |

---

## 9. 성능/렌더링 기준

| PRD 규칙 | 관련 테스트 | 상태 |
|----------|-------------|------|
| 배치 계산 결정적·빠름 | `GridEnginePerformanceSmokeTest` | 신규 |
| 애니메이션 완료 상태 검증 | `GridLayoutInstrumentedTest` | 신규 |
