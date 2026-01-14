# Issue 1 설계: 편집 모드 로딩 및 사진 유지/삭제 처리

## 1) 구현 순서 (Step-by-step)
1. Edit 모드 진입 조건 정의
   - `DiaryId` 전달 시 Edit 모드로 판단
2. 기존 Diary/DiaryPhoto 로딩
   - Repository에서 Diary + Photo 리스트 조회
3. 편집 화면 초기 상태 구성
   - 텍스트 필드에 기존 값 바인딩
   - 사진 리스트를 “유지 상태”로 초기화
4. 사진 삭제 UI 동작 정의
   - 삭제 버튼 클릭 시 상태만 변경 (즉시 DB 삭제 X)
5. 저장 시 변경 사항 적용
   - 유지된 사진은 그대로 유지
   - 삭제 표시된 사진만 제거 처리
6. 저장 후 화면 종료/상태 초기화

## 2) 필요한 데이터 구조 변경
### Diary
- 기존 구조 유지 가능
- 변경 필요 시: 편집 상태와 무관하게 도메인 모델은 순수 데이터 유지

### DiaryPhoto
- 기존 구조 유지 가능
- UI 편집 상태를 위한 별도 모델 필요
  - 예: `UiDiaryPhoto(id, uri, state)` 형태
  - `state`: KEEP | DELETE (추가로 NEW 필요 시 확장)

### photoUris 처리 방식
- 현재: 저장 시 `List<String>`만 전달
- 편집 모드에서는 “기존 + 신규 + 삭제” 구분 필요
- 권장 방식:
  - UI에서는 `UiDiaryPhoto` 리스트 유지
  - 저장 시:
    - `keptExisting`: 기존 사진 중 KEEP
    - `deletedExisting`: 기존 사진 중 DELETE
    - `newPhotos`: 새로 추가된 URI
  - Repository에 명시적으로 전달하거나, ViewModel에서 분리 계산 후 처리

## 3) ViewModel 책임 분리 기준
- ViewModel은 **UI 상태 관리 + 편집 상태 계산**만 담당
- Repository는 **CRUD 책임**만 담당
- 분리 기준:
  - ViewModel: “무엇이 유지/삭제/추가인지” 결정
  - Repository: “실제 DB 및 파일 처리” 수행
- 필요 시 UseCase 계층 분리 고려:
  - `LoadDiaryForEditUseCase`
  - `SaveEditedDiaryUseCase`

## 4) 예상 변경 파일 목록
- `presentation/diary/write/WriteScreen` (편집 모드 UI 로딩/삭제 처리)
- `presentation/diary/DiaryViewModel` (편집 모드 상태/로딩/저장 로직)
- `domain/diary/DiaryPhoto` (유지 or 새 UI 모델 추가)
- `data/diary/repository/...` (편집 저장 로직 추가)
- `data/diary/dao/...` (삭제 처리용 쿼리 필요 시)
- `data/diary/entity/...` (스키마 변경 없으면 수정 불필요)
- (선택) `presentation/diary/write/` 내 UI 상태 모델 파일

## 5) 엣지 케이스 목록
- 기존 사진 0장 → 텍스트만 편집 가능
- 기존 사진 전부 삭제 → 저장 후 사진 0장 상태 유지
- 일부 삭제 + 일부 유지 → 삭제된 항목만 반영
- 신규 사진만 추가 → 기존 사진 유지 + 신규 추가
- 기존 사진 삭제 후 다시 추가 (중복 URI) → 중복 처리 기준 필요
- 저장 중 빠르게 종료 → 변경 사항 누락 가능 (저장 완료 시점 처리 필요)
