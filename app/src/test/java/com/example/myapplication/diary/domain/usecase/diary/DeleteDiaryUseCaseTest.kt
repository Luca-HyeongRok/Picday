package com.example.myapplication.diary.domain.usecase.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.picday.diary.fakes.FakeDiaryRepository
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
    private lateinit var deleteDiaryUseCase: DeleteDiaryUseCase

    @Before
    fun setUp() {
        // 각 테스트 전에 Fake Repository와 UseCase를 초기화합니다.
        fakeDiaryRepository = FakeDiaryRepository()
        deleteDiaryUseCase = DeleteDiaryUseCase(
            diaryRepository = fakeDiaryRepository
        )
    }

    @Test
    fun `invoke_whenDiaryExists_deletesDiaryAndCoverPhotoFromRepositories`() = runTest {
        // Arrange (준비)
        val testDate = LocalDate.of(2025, 1, 1)
        val testCoverUri = "content://com.picday.diary/test_uri"
        val testDiary = Diary(
            id = "test_id_123",
            date = testDate,
            title = "테스트 일기",
            content = "내용",
            createdAt = System.currentTimeMillis(),
            coverPhotoUri = testCoverUri // 다이어리에 커버 사진 URI를 직접 설정
        )

        // Fake Repository에 테스트 데이터를 미리 넣어둡니다.
        fakeDiaryRepository.addDiary(testDiary)

        // 초기 상태 검증 (커버 사진이 정상적으로 설정되었는지)
        assertEquals(testCoverUri, fakeDiaryRepository.getDateCoverPhotoUri(testDate).first())


        // Act (실행)
        deleteDiaryUseCase(testDiary.id)


        // Assert (검증)
        // 1. 다이어리가 DiaryRepository에서 삭제되었는지 확인
        val diaryAfterDelete = fakeDiaryRepository.getDiaryById(testDiary.id)
        assertNull("다이어리가 삭제되지 않았습니다.", diaryAfterDelete)

        // 2. 커버 사진이 DiaryRepository에서 삭제(null)되었는지 확인
        // UseCase는 diaryRepository.setDateCoverPhotoUri를 호출하여 커버를 null로 설정해야 함.
        val coverPhotoAfterDelete = fakeDiaryRepository.getDateCoverPhotoUri(testDate).first()
        assertNull("커버 사진 설정이 삭제되지 않았습니다.", coverPhotoAfterDelete)
    }
}
