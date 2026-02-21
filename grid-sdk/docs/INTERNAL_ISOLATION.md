# Internal 격리 확인

내부 구현이 외부에 노출되지 않도록 하는 격리 상태를 정리합니다.

---

## 1. 격리 원칙

| 구분 | 패키지 | 가시성 | 외부 노출 |
|------|--------|--------|-----------|
| **공개 API** | `com.android.gridsdk.library`, `...model`, `...model.engine` | `public` | ✅ 허용 |
| **내부 구현** | `com.android.gridsdk.library.internal.*` | `internal` | ❌ 차단 |

---

## 2. Internal 패키지 구성

```
internal/
├── InternalApi.kt           # 내부 마커 어노테이션
├── interaction/
│   └── ResizeInteractionState.kt
├── ui/
│   ├── GridLayoutInternal.kt
│   ├── DragGestureHandler.kt
│   ├── ResizeGestureHandler.kt
│   └── EngineStateBridge.kt
├── engine/
│   ├── PlacementExplorer.kt
│   ├── CandidateValidator.kt
│   ├── CandidateScorer.kt
│   └── RollbackEvaluator.kt
├── state/
│   ├── OccupancyGrid.kt
│   ├── RelocatedItemTracker.kt
│   └── StateSnapshot.kt
└── util/
    ├── ResizeSpanCalculator.kt
    └── ValidationUtils.kt
```

모든 위 클래스/object는 `internal` 가시성을 가집니다.

---

## 3. 격리 검증 항목

| 항목 | 상태 |
|------|------|
| `internal` 패키지 클래스에 `internal` 수정자 적용 | ✅ |
| `@InternalApi` 어노테이션으로 내부 타입 표시 | ✅ |
| 공개 API 시그니처에 internal 타입 미포함 | ✅ |
| sample-app 등 consumer가 internal import 불가 | ✅ (Kotlin 모듈 경계) |
| consumer-rules.pro에서 internal repackage | ✅ |

---

## 4. 공개 API → Internal 의존 방향

```
GridLayout (public)
    └── GridLayoutInternal (internal)

GridEngine (public)
    └── PlacementExplorer, RollbackEvaluator (internal)

EngineStateBridge (internal)
    └── GridEngine, RelocatedItemTracker (internal)
```

- **Public → Internal**: 허용 (GridLayout이 GridLayoutInternal 사용)
- **Internal → Public**: 허용 (internal이 GridItem, GridSize 등 공개 모델 사용)
- **Internal → Internal**: 허용

---

## 5. ProGuard/R8

`consumer-rules.pro`:

```
-repackageclasses 'com.android.gridsdk.library.internal'
```

릴리즈 빌드 시 internal 클래스를 리패키징하여 스택 트레이스 등에서 내부 구조 노출을 최소화합니다.
