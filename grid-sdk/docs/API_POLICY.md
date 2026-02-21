# GridSdk API 정책

공개 API 설계 시 적용하는 정책입니다.

---

## 1. 실험 API (`@Experimental`) 정책

### 현재 상태

- **0.1.x**: 모든 공개 API는 안정(stable)으로 간주합니다.
- `@ExperimentalGridSdkApi` 등 실험 마커는 **현재 사용하지 않습니다**.

### 정책

| 상황 | 정책 |
|------|------|
| 새 기능이 아직 피드백 수집 중 | `@ExperimentalGridSdkApi` 마커 추가, KDoc에 "실험적" 명시 |
| 실험 API가 안정화됨 | 마이너 버전(0.x.0)에서 마커 제거, 일반 API로 전환 |
| 실험 API 폐기 | 마이너 버전에서 `@Deprecated` → 다음 마이너에서 제거 |

### 실험 API 도입 시 예시

```kotlin
@RequiresOptIn(message = "This API is experimental and may change.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalGridSdkApi

@ExperimentalGridSdkApi
fun experimentalFeature(): Result
```

---

## 2. 파라미터 기본값 정책

| 규칙 | 설명 |
|------|------|
| **선택적 파라미터** | 기본값을 제공하여 호출 편의성 확보 |
| **Composable** | `modifier`, `cellContent` 등은 기본값 제공 (`Modifier`, `{}`) |
| **콜백** | `onFailure` 등 실패 처리 콜백은 `null` 기본값 허용 |
| **필수 파라미터** | `gridSize`, `items`, `onItemsChange` 등 핵심 인자는 기본값 없음 |

### 현재 적용 예시

```kotlin
@Composable
fun GridLayout(
    gridSize: GridSize,           // 필수
    items: List<GridItem>,       // 필수
    onItemsChange: (List<GridItem>) -> Unit,  // 필수
    modifier: Modifier = Modifier,             // 기본값
    onFailure: ((GridError) -> Unit)? = null, // nullable, 기본 null
    cellContent: @Composable (GridItem) -> Unit = {}  // 기본 빈 콘텐츠
)
```

---

## 3. Nullable 정책

| 규칙 | 설명 |
|------|------|
| **콜백** | 실패/선택 처리 콜백은 `nullable` (`(T) -> Unit)?` |
| **반환값** | 엔진/유틸은 `null` 대신 `Result`/`sealed class` 사용 권장 |
| **데이터 모델** | `GridItem`, `GridSize` 등은 non-null. 빈 값은 `emptyList()` 등으로 표현 |
| **에러** | `EngineResult.Failure(error)`로 명시적 실패 전달 |

### 적용 예시

```kotlin
// Good: nullable 콜백
onFailure: ((GridError) -> Unit)? = null

// Good: sealed class로 성공/실패 구분
sealed class EngineResult {
    data class Success(...) : EngineResult()
    data class Failure(val error: GridError) : EngineResult()
}

// Avoid: nullable 반환 (대신 Result/Sealed 사용)
fun findItem(id: String): GridItem?  // X
```

---

## 4. 공개 최소화 원칙

- 공개 타입은 **PUBLIC_API.md**에 명시된 것만 유지
- 내부 구현은 `internal` 패키지에 배치
- `InternalApi` 어노테이션으로 내부 전용 타입 표시
