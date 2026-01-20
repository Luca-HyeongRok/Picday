# PicDay


로그인 없이 사용하는 개인용 캘린더 다이어리 앱입니다.  
날짜별 기록을 추가/조회/수정하고 사진을 함께 저장할 수 있습니다.  
**1차 출시용 기준으로 현재 구현된 기능과 구조를 정리했습니다.**

<h2>📸 App Screenshots</h2>
<table>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/6e8537f9-02c7-4480-98ab-29907cfd46ad" width="200" alt="Calendar"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/4e1b55af-175b-49d2-8f49-4f5e165a56fe" width="200" alt="Diary"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/093c2779-7e35-451b-be16-1bc9daa79f74" width="200" alt="Detail"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/5afeca62-a828-421b-b978-ebf3dc63c383" width="200" alt="Write"/>
    </td>
  </tr>
  <tr>
    <td align="center"><b>Calendar</b></td>
    <td align="center"><b>Diary</b></td>
    <td align="center"><b>Detail</b></td>
    <td align="center"><b>Write</b></td>
  </tr>
</table>

## 주요 기능 (현재 가능한 기능)
- 캘린더 날짜 선택 → 해당 날짜 기록 보기/작성
- 작성 화면에서 추가/수정 모드 지원
- 한 기록에 여러 장의 사진(1:N) 첨부 및 썸네일 미리보기
- 날짜별 기록 리스트 제공, 최신 기록 강조 표시

## 아키텍처 / 패턴
- 계층 분리: `domain` / `data` / `presentation`
- Repository 패턴: `DiaryRepository` + `Room/InMemory` 구현체
- Hilt DI로 Repository 및 ViewModel 주입
- 단일 `DiaryViewModel`을 Calendar/Diary/Write 화면에서 공유
- Navigation3 기반 단일 루트 네비게이션
- 네비게이션 상태 전이를 순수 리듀서로 분리
- 네비게이션은 단위 테스트로 backStack / effect만 검증 (UI 테스트 제외)

## 전체 구조
```
presentation
├─ calendar
├─ diary
│ ├─ write
│ └─ DiaryViewModel
├─ navigation
└─ component

domain
├─ diary
│ ├─ Diary
│ └─ DiaryPhoto
└─ repository

data
├─ diary
│ ├─ entity
│ ├─ dao
│ ├─ database
│ └─ repository
```

## MVVM 패턴
- **View**: Compose 화면 (`CalendarScreen`, `DiaryScreen`, `WriteScreen`)
- **ViewModel**: `DiaryViewModel` (UI 상태 및 사용자 이벤트 처리)
- **Model**: `Diary`, `DiaryPhoto` (도메인 모델)
    + Repository + Room(Entity / DAO)

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
- `diary_photo` (1:N 관계)

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
- Room 기반 로컬 영속성 저장
- debug 빌드: InMemory Repository + 시드 데이터
- release 빌드: Room Repository만 사용
- WriteScreen에서 다중 사진 추가/수정 및 썸네일 미리보기 지원
- 카드형 리스트, 둥근 버튼/입력창, 하단 네비게이션 UI 적용

## 향후 계획 (아직 미구현 또는 고도화 예정)
- 카메라 촬영 기능 추가 및 갤러리 UX 개선
- 편집 모드 사진 diff 처리 로직 고도화
- 썸네일 캐싱 등 미디어 성능 최적화
- 작성/편집 UI 인터랙션 및 에러 처리 보완

## 다음에 할 것들
1) EDIT 모드 사진 로딩/상태 처리 구조 정리
2) 사진 삭제 시 사용자 확인 UX 추가
3) Room 마이그레이션 전략 수립
4) 작성 플로우 에러 처리 및 빈 상태 UX 개선  
