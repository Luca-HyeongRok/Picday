package com.example.myapplication.diary.domain.usecase.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DeleteDiaryUseCaseTest {

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var deleteDiaryUseCase: DeleteDiaryUseCase

    @Before
    fun setUp() {
        // 각 테스트 전에 Fake Repository와 UseCase를 초기화합니다.
        fakeDiaryRepository = FakeDiaryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        deleteDiaryUseCase = DeleteDiaryUseCase(
            diaryRepository = fakeDiaryRepository,
            settingsRepository = fakeSettingsRepository
        )
    }

    @Test
    fun `invoke_whenDiaryExists_deletesDiaryAndCoverPhotoFromRepositories`() = runTest {
        // Arrange (준비)
        val testDate = LocalDate.of(2025, 1, 1)
        val testDiary = Diary(
            id = "test_id_123",
            date = testDate,
            title = "테스트 일기",
            content = "내용",
            createdAt = System.currentTimeMillis()
        )
        val testCoverUri = "content://com.picday.diary/test_uri"

        // Fake Repository들에 테스트 데이터를 미리 넣어둡니다.
        // FakeDiaryRepository는 addDiaryForDate 시 랜덤 ID를 생성하므로, 직접 내부 list에 접근합니다.
        // (만약 Fake가 이를 허용하지 않는다면, getByDate 등으로 다시 조회해서 ID를 얻어야 합니다)
        // -> 현재 FakeDiaryRepository는 내부 list가 private이므로, getByDate로 조회하는 방식을 사용하겠습니다.
        fakeDiaryRepository.addDiaryForDate(testDate, testDiary.title, testDiary.content)
        val addedDiary = fakeDiaryRepository.getByDate(testDate).first()
        assertNotNull("테스트 데이터 준비 실패", addedDiary)

        fakeSettingsRepository.setDateCoverPhotoUri(testDate, testCoverUri)

        // 초기 상태 검증 (커버 사진이 정상적으로 설정되었는지)
        assertEquals(testCoverUri, fakeSettingsRepository.getDateCoverPhotoUri(testDate).first())


        // Act (실행)
        deleteDiaryUseCase(addedDiary.id)


        // Assert (검증)
        // 1. 다이어리가 DiaryRepository에서 삭제되었는지 확인
        val diaryAfterDelete = fakeDiaryRepository.getDiaryById(addedDiary.id)
        assertNull("다이어리가 삭제되지 않았습니다.", diaryAfterDelete)

        // 2. 커버 사진이 SettingsRepository에서 삭제(null)되었는지 확인
        val coverPhotoAfterDelete = fakeSettingsRepository.getDateCoverPhotoUri(testDate).first()
        assertNull("커버 사진 설정이 삭제되지 않았습니다.", coverPhotoAfterDelete)
    }
}
