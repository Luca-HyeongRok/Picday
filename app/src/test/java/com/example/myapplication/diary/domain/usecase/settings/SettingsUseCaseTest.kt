package com.example.myapplication.diary.domain.usecase.settings

import app.cash.turbine.test
import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.usecase.settings.GetDateCoverPhotoUseCase
import com.picday.diary.domain.usecase.settings.ObserveCalendarBackgroundUseCase
import com.picday.diary.domain.usecase.settings.SetCalendarBackgroundUseCase
import com.picday.diary.domain.usecase.settings.SetDateCoverPhotoUseCase
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCaseTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var diaryRepository: FakeDiaryRepository // 추가: DiaryRepository
    private lateinit var observeCalendarBackground: ObserveCalendarBackgroundUseCase
    private lateinit var setCalendarBackground: SetCalendarBackgroundUseCase
    private lateinit var getDateCoverPhoto: GetDateCoverPhotoUseCase
    private lateinit var setDateCoverPhoto: SetDateCoverPhotoUseCase

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        diaryRepository = FakeDiaryRepository() // 초기화: DiaryRepository
        observeCalendarBackground = ObserveCalendarBackgroundUseCase(settingsRepository)
        setCalendarBackground = SetCalendarBackgroundUseCase(settingsRepository)
        getDateCoverPhoto = GetDateCoverPhotoUseCase(diaryRepository) // 수정: diaryRepository 주입
        setDateCoverPhoto = SetDateCoverPhotoUseCase(diaryRepository) // 수정: diaryRepository 주입
    }

    @Test
    fun `캘린더 배경 설정이 flow에 반영되어야 한다`() = runTest {
        observeCalendarBackground().test {
            assertEquals(null, awaitItem())
            setCalendarBackground("uri_bg")
            assertEquals("uri_bg", awaitItem())
        }
    }

    @Test
    fun `날짜별 대표 사진 설정이 flow에 반영되어야 한다`() = runTest {
        val date = LocalDate.of(2024, 5, 1)
        // 테스트를 위해 해당 날짜의 다이어리를 먼저 추가
        val testDiary = Diary(
            id = "test_diary_id",
            date = date,
            title = "Test Title",
            content = "Test Content",
            createdAt = System.currentTimeMillis()
        )
        diaryRepository.addDiary(testDiary) // addDiary는 FakeDiaryRepository에 추가된 public 메서드

        getDateCoverPhoto(date).test {
            assertEquals(null, awaitItem())
            setDateCoverPhoto(date, "uri_cover")
            assertEquals("uri_cover", awaitItem())
        }
    }
}
