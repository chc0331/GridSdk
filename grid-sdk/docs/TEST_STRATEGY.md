# GridSdk 테스트 전략

GridSdk의 테스트 전략, 실행 방법, CI 연동 가이드입니다.

---

## 1. 테스트 구조 개요

| 테스트 유형 | 위치 | 실행 환경 | 목적 |
|-------------|------|-----------|------|
| JVM 단위 테스트 | `grid-sdk/src/test/` | 로컬 JVM (디바이스 불필요) | 코어 엔진, 모델, 유틸 검증 |
| Instrumented 테스트 | `sample-app/src/androidTest/` | 에뮬레이터/기기 | Compose UI, 제스처, 애니메이션 검증 |

---

## 2. JVM 단위 테스트 실행

코어 엔진 및 순수 로직은 JVM에서 실행되며, CI/로컬에서 디바이스 없이 실행 가능합니다.

```bash
# grid-sdk 전체 단위 테스트
./gradlew :grid-sdk:testDebugUnitTest

# 전체 프로젝트 테스트
./gradlew testDebugUnitTest
```

**테스트 결과**: `grid-sdk/build/reports/tests/testDebugUnitTest/index.html`

---

## 3. Instrumented 테스트 실행

Compose UI 테스트는 Android 기기/에뮬레이터에서 실행됩니다.

```bash
# sample-app instrumented 테스트 (기기/에뮬레이터 연결 필요)
./gradlew :sample-app:connectedDebugAndroidTest
```

---

## 4. 테스트 카테고리

| 카테고리 | 파일 | 설명 |
|----------|------|------|
| 엔진 Add | `GridEngineAddTest` | 아이템 추가, 상단-좌측 배치, GridFull/DuplicateId 실패 |
| 엔진 Move | `GridEngineMoveTest` | 이동, 충돌 재배치, OutOfBounds, NoFeasibleLayout |
| 엔진 Resize | `GridEngineResizeTest` | 리사이즈, 충돌 재배치, InvalidItem 실패 |
| 엔진 Rollback | `GridEngineRollbackTest` | 드래그 중 원위치 복귀 |
| Property | `GridEnginePropertyTest` | 랜덤 시나리오로 충돌 탐색 안정성 검증 |
| 회귀 | `GridEngineGridSizeRegressionTest` | N/M 변경 시 동작 검증 |
| 상태 불변 | `GridEngineStateInvariantTest` | 실패 시 items 불변 검증 |
| 롤백 회귀 | `GridEngineRollbackRegressionTest` | 롤백 규칙 회귀 검증 |
| 성능 | `GridEnginePerformanceSmokeTest` | 아이템 수 증가 시 성능 smoke |
| UI | `GridLayoutInstrumentedTest` | Compose 제스처 + 애니메이션 완료 상태 |

---

## 5. CI 연동

### GitHub Actions 예시

```yaml
- name: Run unit tests
  run: ./gradlew :grid-sdk:testDebugUnitTest --no-daemon

- name: Run instrumented tests (optional, requires emulator)
  run: ./gradlew :sample-app:connectedDebugAndroidTest
```

### 권장 사항

- **필수**: JVM 단위 테스트는 모든 PR에서 실행
- **선택**: Instrumented 테스트는 에뮬레이터 사용 가능 시 실행
- **성능 테스트**: Smoke 수준으로 초기에는 느슨한 기준 적용

---

## 6. 관련 문서

- [TEST_MATRIX.md](TEST_MATRIX.md) — PRD 규칙별 테스트 매트릭스
- [INVARIANTS.md](../INVARIANTS.md) — 그리드 불변조건
