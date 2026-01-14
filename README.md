# PicDay

캘린더 기반의 일기 작성 앱입니다. 날짜별 기록 추가/조회/수정을 간단한 UX로 제공합니다.

## 주요 기능
- 캘린더 날짜 선택 → 해당 날짜 기록 보기/작성
- 작성 화면에서 추가/수정 모드 지원
- 한 기록에 여러 장의 사진(1:N) 첨부 및 썸네일 미리보기
- 날짜별 기록 리스트 제공, 최신 기록 강조 표시

## 아키텍처 / 패턴
- 계층 분리: `domain` / `data` / `presentation`
- Repository 패턴: `DiaryRepository` + `Room/InMemory` 구현체
- Hilt DI로 Repository 및 ViewModel 주입
- 단일 `DiaryViewModel`을 Calendar/Diary/Write 화면에서 공유

## 전체 구조
```
presentation
 ├─ calendar
 ├─ diary
 │   ├─ write
 │   └─ DiaryViewModel
 ├─ navigation
 └─ component

domain
 ├─ diary
 │   ├─ Diary
 │   └─ DiaryPhoto
 └─ repository

data
 ├─ diary
 │   ├─ entity
 │   ├─ dao
 │   ├─ database
 │   └─ repository

```

## MVVM 패턴
- **View**: Compose 화면(`CalendarScreen`, `DiaryScreen`, `WriteScreen`)
- **ViewModel**: `DiaryViewModel` (UI 상태/이벤트 처리)
- **Model**: `Diary`/`DiaryPhoto` (도메인 모델), Repository + Room(Entity/DAO)

## 데이터 구조
### 도메인 모델
- `Diary`
  - id: String
  - date: LocalDate
  - title: String?
  - content: String
  - createdAt: Long
- `DiaryPhoto`
  - id: String
  - diaryId: String
  - uri: String
  - createdAt: Long

### Room 테이블
- `diary`
- `diary_photo` (1:N)

## 개발 환경
| 항목 | 값 |
| --- | --- |
| 언어 | Kotlin |
| UI | Jetpack Compose |
| DB | Room 2.6.1 |
| DI | Hilt 2.51.1 |
| minSdk | 24 |
| targetSdk | 36 |
| Java | 11 |

## 현재 상태
- Room 영속성 적용 + Hilt 연동 완료
- debug 빌드: InMemory + 시드 데이터 사용
- release 빌드: Room만 사용
- WriteScreen에서 다중 사진 추가/수정 및 썸네일 미리보기 지원

## 문서
- [PRD](docs/PRD.md)

## Issue Templates
- GitHub에서 새 이슈 생성 시 템플릿을 선택해 사용한다.
- 작업 성격에 맞는 템플릿을 고른 뒤 제목과 체크리스트를 업데이트한다.

## Design Docs
- [Issue 1 – Edit Mode Design](docs/design/issue-1-edit-mode.md)

## ✅ QA / Verification
- [Manual QA Checklist v1](docs/qa/manual-checklist-v1.md)

## 향후 계획
- 카메라 촬영 기능 추가 및 갤러리 개선
- 편집 모드 사진 diff 처리(유지/삭제/추가) 고도화
- 썸네일 캐싱 등 미디어 성능 최적화
- 작성/편집 UI 디테일 개선

## 다음에 할 것들
1) EDIT 모드 사진 로딩 비동기 처리 개선  
2) 사진 삭제 반영 로직 및 확인 UX 추가  
3) Room 마이그레이션 전략 수립  
4) 작성 플로우 에러 처리 및 빈 상태 UX 개선
