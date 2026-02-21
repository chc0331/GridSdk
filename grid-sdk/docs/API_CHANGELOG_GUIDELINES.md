# GridSdk API 변경 가이드라인 (Semver 기준)

버전 번호 형식: `MAJOR.MINOR.PATCH` (예: `0.1.0`)

---

## 1. Semver 규칙 요약

| 변경 유형 | 버전 증가 | 예시 |
|-----------|-----------|------|
| **Breaking change** (호환성 깨짐) | MAJOR | 공개 함수 시그니처 변경, 타입 제거 |
| **기능 추가** (하위 호환) | MINOR | 새 함수, 새 sealed subclass, 새 파라미터(기본값 있음) |
| **버그 수정, 문서 개선** | PATCH | 동작 수정(스펙 유지), KDoc 수정 |

---

## 2. 0.x.y (초기 개발) 특례

- **0.x.y**에서는 MINOR 버전 증가 시 breaking change 허용
- 1.0.0 도달 전까지는 공개 API가 불안정할 수 있음을 문서에 명시

---

## 3. Breaking Change 정의

다음은 **breaking change**로 간주합니다:

| 항목 | 설명 |
|------|------|
| 함수/클래스 제거 | 공개 API에서 제거 |
| 파라미터 제거 | 기존 파라미터 삭제 |
| 파라미터 타입 변경 | `String` → `Int` 등 |
| 파라미터 순서 변경 | 기본값 없는 파라미터 순서 변경 |
| 반환 타입 변경 | `List` → `Set` 등 |
| Sealed class 서브클래스 제거 | `GridError` 하위 타입 제거 |
| 동작 변경으로 기존 호출부 오동작 | 스펙상 허용되던 사용이 실패하도록 변경 |

---

## 4. 하위 호환 변경 (Non-Breaking)

다음은 **하위 호환**으로 간주합니다:

| 항목 | 설명 |
|------|------|
| 새 함수/클래스 추가 | 기존 API에 영향 없음 |
| 새 파라미터 추가 (기본값 있음) | `fun foo(x: Int, y: Int = 0)` |
| Sealed class 새 서브클래스 추가 | `when`에 `else` 있으면 무관 |
| Deprecation 추가 | `@Deprecated` 표시, 동작 유지 |
| KDoc 수정 | 문서만 변경 |
| 내부 구현 변경 | public API 시그니처/동작 유지 |

---

## 5. Deprecation 정책

1. **Deprecate**: `@Deprecated(message = "...", replaceWith = ReplaceWith("..."))` 추가
2. **유지**: 최소 1개 MINOR 버전 동안 동작 유지
3. **제거**: 다음 MAJOR(또는 0.x에서 다음 MINOR)에서 제거

---

## 6. 체크리스트 (릴리즈 전)

- [ ] `PUBLIC_API.md`와 실제 공개 타입 일치 여부 확인
- [ ] 변경 사항이 Breaking인지 Non-Breaking인지 분류
- [ ] 버전 번호 증가 규칙 적용 (MAJOR/MINOR/PATCH)
- [ ] CHANGELOG.md에 변경 내역 기록
- [ ] Deprecation 시 마이그레이션 가이드 제공

---

## 7. 예시

| 변경 | 버전 | 이유 |
|------|------|------|
| `GridLayout`에 `onDragStart` 파라미터 추가 (기본값 `null`) | 0.1.0 → 0.2.0 | 새 기능, 하위 호환 |
| `GridError.ItemOverlap` 제거 | 0.1.0 → 1.0.0 | Breaking |
| `GridItem.resize` 반환 타입 `GridItem` 유지, 내부 로직 수정 | 0.1.0 → 0.1.1 | PATCH, 버그 수정 |
