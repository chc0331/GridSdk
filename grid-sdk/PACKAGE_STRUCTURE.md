# GridSdk Package Structure

## Public API Packages
외부에 공개되는 API는 다음 패키지들에만 위치합니다:

```
com.android.gridsdk.library
├── GridLayout.kt           # 메인 Composable 컴포넌트
├── GridSdkInfo.kt          # 버전/이름 상수
├── model/                  # 공개 데이터 모델
│   ├── GridSize.kt
│   ├── GridItem.kt
│   ├── GridError.kt
│   └── engine/
│       ├── GridEngine.kt   # 엔진 진입점
│       ├── EngineRequests.kt
│       └── EngineResults.kt
```

### 공개 규칙:
- 모든 public 클래스와 함수는 위 패키지 구조를 따릅니다
- 공개 API는 최소한으로 유지합니다
- 모든 공개 API는 KDoc 문서화가 필수입니다

## Internal Packages
내부 구현은 `internal` 패키지에 위치하며, 외부에서 접근할 수 없습니다:

```
com.android.gridsdk.library.internal
├── InternalApi.kt          # 내부 마커 어노테이션
├── ui/                     # UI/제스처 구현
│   ├── GridLayoutInternal.kt
│   ├── DragGestureHandler.kt
│   ├── ResizeGestureHandler.kt
│   └── EngineStateBridge.kt
├── engine/                 # 배치 엔진 코어
│   ├── PlacementExplorer.kt
│   ├── CandidateValidator.kt
│   ├── CandidateScorer.kt
│   └── RollbackEvaluator.kt
├── state/                  # 내부 상태 관리
│   ├── OccupancyGrid.kt
│   ├── RelocatedItemTracker.kt
│   └── StateSnapshot.kt
├── interaction/
│   └── ResizeInteractionState.kt
└── util/
    ├── ResizeSpanCalculator.kt
    └── ValidationUtils.kt
```

### Internal 규칙:
- 모든 내부 클래스는 `internal` 가시성 수정자를 사용합니다
- 내부 구현 세부사항은 외부에서 접근할 수 없습니다
- Kotlin의 `internal` 키워드를 활용하여 모듈 외부 접근을 차단합니다

## 코드 작성 규칙

### Public API
```kotlin
// 공개 API 예시
package com.android.gridsdk.library

/**
 * GridLayout을 구성하는 메인 Composable
 */
@Composable
public fun GridLayout(
    gridSize: GridSize,
    items: List<GridItem>,
    onItemsChange: (List<GridItem>) -> Unit,
    modifier: Modifier = Modifier,
    onFailure: ((GridError) -> Unit)? = null,
    cellContent: @Composable (GridItem) -> Unit = {}
) {
    // implementation
}
```

### Internal API
```kotlin
// 내부 구현 예시
package com.android.gridsdk.library.internal.engine

/**
 * 아이템 배치 알고리즘 구현 (내부 전용)
 */
internal class PlacementAlgorithm {
    internal fun findOptimalPosition(
        item: GridItem,
        occupancy: OccupancyGrid
    ): Position? {
        // implementation
    }
}
```

## 파일 네이밍 규칙
- Public API: PascalCase, 명확한 이름 (예: `GridLayout.kt`, `GridState.kt`)
- Internal: PascalCase, 구현 세부사항 (예: `LayoutEngine.kt`, `OccupancyGrid.kt`)

## 의존성 규칙
- Public API는 internal 패키지에 의존해도 됩니다 (private 멤버로 사용)
- Internal 패키지는 절대 public API에 의존하지 않습니다
- Internal 간 의존성은 허용되지만, 순환 의존성은 금지됩니다
