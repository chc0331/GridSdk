# GridSdk

Jetpack Compose 기반 N×M 고정 그리드 레이아웃 SDK입니다. 아이템의 추가, 삭제, 드래그 이동, 롱프레스 후 핸들러 드래그 리사이즈를 지원하며, 충돌 시 자동 재배치와 롤백 규칙을 적용합니다.

## 요구사항

- Android minSdk 34+
- Kotlin 2.0+
- Jetpack Compose
- Java 17

## 설치

### 로컬 Maven (개발용)

```bash
./gradlew :grid-sdk:publishToMavenLocal
```

`settings.gradle.kts`에 `mavenLocal()` 추가 후:

```kotlin
implementation("com.android.gridsdk:grid-sdk:0.1.0")
```

### 프로젝트 의존성 (개발 중)

```kotlin
implementation(project(":grid-sdk"))
```

## 빠른 시작

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

### 아이템 추가

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
    is EngineResult.Failure -> { /* result.error 처리 */ }
}
```

- **드래그**: 아이템 이동 (충돌 시 자동 재배치)
- **롱프레스**: 리사이즈 핸들러 표시 → 핸들러 드래그로 아이템 리사이즈

## 샘플 앱 실행

```bash
./gradlew :sample-app:installDebug
```

또는 Android Studio에서 `sample-app` 모듈을 실행하세요.

- **Basic**: 추가/삭제/이동/리사이즈 데모
- **Edge Case**: NoFeasibleLayout, 롤백 시나리오
- **N/M Change**: 런타임 그리드 크기 변경

## 문서

| 문서 | 설명 |
|------|------|
| [grid-sdk/docs/PUBLIC_API.md](grid-sdk/docs/PUBLIC_API.md) | 공개 API 목록 |
| [grid-sdk/docs/API_EXAMPLES.md](grid-sdk/docs/API_EXAMPLES.md) | 사용 예제 |
| [grid-sdk/docs/API_REFERENCE.md](grid-sdk/docs/API_REFERENCE.md) | API 레퍼런스 |
| [grid-sdk/docs/LIMITATIONS.md](grid-sdk/docs/LIMITATIONS.md) | 제한사항 / Known Issues |
| [docs/HLA_ARCHITECTURE.md](docs/HLA_ARCHITECTURE.md) | 아키텍처 개요 |
| [BUILD_GUIDE.md](BUILD_GUIDE.md) | 빌드 및 배포 가이드 |

## 라이선스

Apache License 2.0
