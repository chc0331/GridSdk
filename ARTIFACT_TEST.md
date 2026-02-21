# Step-by-Step: Testing Published SDK Artifact

## Current State
- `:sample-app` uses: `implementation(project(":grid-sdk"))`
- This is the **development mode** (직접 의존)

## Testing Published Artifact

### Step 1: Publish SDK to Maven Local
Android Studio에서:
1. Gradle 창 열기
2. `grid-sdk > Tasks > publishing > publishToMavenLocal` 실행

또는 터미널:
```bash
./gradlew :grid-sdk:publishToMavenLocal
```

### Step 2: Enable Maven Local Repository

`settings.gradle.kts` 수정:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()  // ← 이 줄 추가
        google()
        mavenCentral()
    }
}
```

### Step 3: Switch to Maven Artifact

`sample-app/build.gradle.kts` 수정:

**Before (개발 모드):**
```kotlin
dependencies {
    implementation(project(":grid-sdk"))
    // ... 다른 의존성들
}
```

**After (배포 테스트 모드):**
```kotlin
dependencies {
    implementation("com.android.gridsdk:grid-sdk:0.1.0")
    // ... 다른 의존성들
}
```

### Step 4: Gradle Sync & Build

1. Android Studio에서 "Sync Project with Gradle Files" 실행
2. `:sample-app` 빌드

### Step 5: Verify

빌드가 성공하면:
- ✓ SDK가 올바르게 패키징되었음
- ✓ 의존성이 올바르게 포함되었음
- ✓ Public API가 정상 노출됨
- ✓ Consumer ProGuard 규칙이 적용됨

## Switching Back to Development Mode

테스트 완료 후 다시 개발 모드로:

1. `sample-app/build.gradle.kts` 복원:
```kotlin
implementation(project(":grid-sdk"))
```

2. (선택) `settings.gradle.kts`에서 `mavenLocal()` 제거

3. Gradle Sync

## Troubleshooting

### 문제: "Failed to resolve: com.android.gridsdk:grid-sdk:0.1.0"

**해결:**
1. Maven Local에 배포되었는지 확인:
   ```bash
   ls -la ~/.m2/repository/com/android/gridsdk/grid-sdk/0.1.0/
   ```

2. `settings.gradle.kts`에 `mavenLocal()` 추가 확인

3. Gradle 캐시 정리 후 재시도:
   ```bash
   ./gradlew clean --refresh-dependencies
   ```

### 문제: "Unresolved reference" 에러

**원인:** SDK의 public API가 제대로 export되지 않음

**해결:**
1. `grid-sdk/build.gradle.kts`에서 의존성 확인
2. 필요한 라이브러리를 `api` 대신 `implementation`으로 설정했는지 확인
3. 샘플 앱에 필요한 의존성을 직접 추가

## Notes

- 개발 중에는 `project(":grid-sdk")` 사용 권장 (즉시 반영)
- 배포 전 최종 검증 시에만 Maven 아티팩트로 테스트
- 버전 변경 시 `grid-sdk/build.gradle.kts`의 `version` 수정 필요
