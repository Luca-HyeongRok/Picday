# PicDay

로그인 없이 사용하는 개인용 캘린더 다이어리 앱입니다.  
날짜 단위로 기록을 남기고 사진을 함께 보관할 수 있습니다.

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

## 설계 의도
- 날짜 기반 기록 앱이므로 **선택 날짜**가 전 화면에서 일관되게 동기화되도록 설계
- 여러 진입점(일반 실행/위젯/딥링크)을 **단일 내비게이션 파이프라인**으로 통합
- 화면 이동과 상태 변경을 **순수 리듀서**로 분리해 예측 가능한 흐름 유지

## 아키텍처 개요
- 계층 분리: `presentation` / `domain` / `data`
- `domain`은 **use case 중심**으로 의존성을 정리하고, `data`는 Room/DAO/Repository로 구현
- Hilt로 ViewModel/Repository/DB 주입
- DataStore로 위젯 커버/배경 설정 저장

## Navigation 구조 (Navigation3 + Reducer)
- Navigation3 기반 `NavDisplay` 사용
- **NavigationRoot**가 모든 이벤트를 받아 reducer(`reduceMainNav`)로 상태를 계산
- reducer는 `NavigationState`와 `NavEffect`를 분리해 **상태와 부수효과를 분리**
- 딥링크 진입 시 백스택을 `Calendar → Diary`로 재구성하여 뒤로가기 동작을 일관되게 보장

## Entry Point 통합
- 일반 실행: `MainActivity` → `NavigationRoot`
- 위젯 클릭: 위젯 날짜 → `start_date` → 딥링크 변환 → `NavigationRoot`
- 딥링크: `app://picday.co/diary/{yyyy-MM-dd}` 형식으로 처리
- 동일 딥링크 반복 호출에도 동작하도록 **timestamp를 덧붙여 이벤트를 강제 갱신**

## 상태 관리
- 화면 상태는 ViewModel의 `StateFlow`로 관리
- 선택 날짜는 `SharedViewModel`에서 전역 공유
- 내비게이션 상태는 reducer가 단일 소스로 관리

## 위젯 설계
- `CalendarWidgetProvider` + `RemoteViewsService` 구성
- 월 상태는 SharedPreferences에 저장, 월 변경 시 데이터 갱신
- 데이터는 **Room DB**에서 직접 조회, 커버 사진은 DataStore 우선 적용
- 썸네일 로딩은 Coil 사용 (`allowHardware(false)`로 RemoteViews 호환)

## 프로젝트 구조
```
app/src/main/java/com/picday/diary
├─ core
│  └─ navigation
├─ data
│  └─ diary
│     ├─ dao
│     ├─ database
│     ├─ entity
│     └─ repository
├─ di
├─ domain
│  ├─ diary
│  ├─ repository
│  └─ usecase
├─ presentation
│  ├─ calendar
│  ├─ common
│  ├─ diary
│  ├─ main
│  ├─ navigation
│  ├─ theme
│  └─ write
└─ widget
```

## 기술 스택
| 항목 | 값 |
| --- | --- |
| 언어 | Kotlin |
| UI | Jetpack Compose |
| Navigation | Navigation3 |
| DB | Room |
| 설정 저장 | DataStore |
| 이미지 로딩 | Coil |
| DI | Hilt |
| minSdk | 24 |
| targetSdk | 36 |
| Java | 11 |

## 향후 작업 (정리된 TODO)
- 편집 모드 사진 diff 처리 로직 정리
- 썸네일 캐싱/리사이징 파이프라인 개선
- Room 마이그레이션 전략 정의
- 작성/편집 오류 처리 및 빈 상태 UX 보강
