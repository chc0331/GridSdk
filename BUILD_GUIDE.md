# GridSdk Build & Publish Guide

## 로컬 빌드 및 배포

### 1. SDK 단독 빌드

#### Debug 빌드
```bash
./gradlew :grid-sdk:assembleDebug
```

#### Release 빌드
```bash
./gradlew :grid-sdk:assembleRelease
```

빌드 산출물 위치:
- `grid-sdk/build/outputs/aar/grid-sdk-debug.aar`
- `grid-sdk/build/outputs/aar/grid-sdk-release.aar`

### 2. 로컬 Maven 저장소에 배포

```bash
./gradlew :grid-sdk:publishToMavenLocal
```

배포 위치: `~/.m2/repository/com/android/gridsdk/grid-sdk/0.1.0/`

또는 프로젝트 내 로컬 저장소에 배포:

```bash
./gradlew :grid-sdk:publishReleasePublicationToLocalRepoRepository
```

배포 위치: `build/repo/com/android/gridsdk/grid-sdk/0.1.0/`

### 3. 샘플 앱에서 로컬 아티팩트 사용

#### 옵션 A: Maven Local 사용

`settings.gradle.kts`에 추가:
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()  // 추가
        google()
        mavenCentral()
    }
}
```

`sample-app/build.gradle.kts`:
```kotlin
dependencies {
    // 프로젝트 의존 대신 Maven 아티팩트 사용
    implementation("com.android.gridsdk:grid-sdk:0.1.0")
}
```

#### 옵션 B: 프로젝트 로컬 저장소 사용

`settings.gradle.kts`에 추가:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("${rootProject.projectDir}/build/repo")
        }
        google()
        mavenCentral()
    }
}
```

## 빌드 검증 체크리스트

- [ ] `:grid-sdk:assembleDebug` 성공
- [ ] `:grid-sdk:assembleRelease` 성공
- [ ] AAR 파일 생성 확인
- [ ] `publishToMavenLocal` 성공
- [ ] 로컬 Maven 저장소에 아티팩트 확인
- [ ] 샘플 앱에서 Maven 아티팩트 의존 빌드 성공

## 버전 관리

현재 버전: `0.1.0` (grid-sdk/build.gradle.kts에서 관리)

버전 업데이트 시:
1. `grid-sdk/build.gradle.kts`의 `version` 수정
2. `GridSdkInfo.kt`의 `VERSION` 상수 수정
3. `CHANGELOG.md` 업데이트 (추후 생성)
