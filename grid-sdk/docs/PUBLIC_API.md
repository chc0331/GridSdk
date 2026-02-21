# GridSdk 공개 API 목록

외부 앱에서 사용할 수 있는 **공개 타입**만 이 문서에 나열합니다.  
이 목록 외의 타입은 내부 구현이며, 버전 간 변경·제거될 수 있습니다.

---

## 1. Composable

| 타입 | 패키지 | 설명 |
|------|--------|------|
| `GridLayout` | `com.android.gridsdk.library` | N×M 그리드 레이아웃 Composable. 드래그 이동, 롱프레스+드래그 리사이즈 지원 |

---

## 2. 데이터 모델

| 타입 | 패키지 | 설명 |
|------|--------|------|
| `GridSize` | `com.android.gridsdk.library.model` | 그리드 크기 (rows × columns) |
| `GridItem` | `com.android.gridsdk.library.model` | 그리드 내 아이템 (id, x, y, spanX, spanY) |

---

## 3. 엔진 API

| 타입 | 패키지 | 설명 |
|------|--------|------|
| `GridEngine` | `com.android.gridsdk.library.model.engine` | 배치 엔진 진입점. `process()`, `evaluateRollback()` |
| `EngineRequest` | `com.android.gridsdk.library.model.engine` | 엔진 요청 (Move, Resize, Add) |
| `EngineResult` | `com.android.gridsdk.library.model.engine` | 엔진 결과 (Success, Failure) |

---

## 4. 에러 타입

| 타입 | 패키지 | 설명 |
|------|--------|------|
| `GridError` | `com.android.gridsdk.library.model` | 실패 원인 sealed class |
| `GridError.OutOfBounds` | - | 경계 초과 |
| `GridError.NoFeasibleLayout` | - | 유효 배치 없음 |
| `GridError.ItemOverlap` | - | 아이템 겹침 |
| `GridError.ItemNotFound` | - | 아이템 미존재 |
| `GridError.InvalidItem` | - | 유효하지 않은 아이템 |
| `GridError.GridFull` | - | 그리드 가득 참 |
| `GridError.DuplicateItemId` | - | 중복 ID |
| `GridError.Position` | - | 위치 정보 (x, y, spanX, spanY) |

---

## 5. 타입 별칭 (선택 사용)

| 별칭 | 실제 타입 |
|------|-----------|
| `MoveRequest` | `EngineRequest.Move` |
| `ResizeRequest` | `EngineRequest.Resize` |
| `AddRequest` | `EngineRequest.Add` |
| `EngineSuccess` | `EngineResult.Success` |
| `EngineFailure` | `EngineResult.Failure` |

---

## 6. 유틸리티

| 타입 | 패키지 | 설명 |
|------|--------|------|
| `GridSdkInfo` | `com.android.gridsdk.library` | 라이브러리 버전/이름 상수 |

---

## 7. 공개하지 않는 타입 (internal)

다음은 **internal** 패키지에 있으며, 외부에서 사용하면 안 됩니다.

- `com.android.gridsdk.library.internal.*` 전체
- `GridLayoutInternal`, `EngineStateBridge`, `DragGestureHandler`, `ResizeGestureHandler`
- `PlacementExplorer`, `OccupancyGrid`, `RelocatedItemTracker`, `RollbackEvaluator` 등

---

## 8. 최소 의존성

일반적인 Compose 앱에서 GridSdk를 사용할 때 필요한 최소 import:

```kotlin
import com.android.gridsdk.library.GridLayout
import com.android.gridsdk.library.model.GridItem
import com.android.gridsdk.library.model.GridSize
import com.android.gridsdk.library.model.GridError
import com.android.gridsdk.library.model.engine.EngineRequest
import com.android.gridsdk.library.model.engine.EngineResult
import com.android.gridsdk.library.model.engine.GridEngine
```
