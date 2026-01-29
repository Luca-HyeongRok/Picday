package com.example.myapplication.diary.domain.usecase.calendar

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.usecase.calendar.ObserveMonthlyDiariesUseCase
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMonthlyDiariesUseCaseTest {

    private lateinit var diaryRepository: FakeDiaryRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var observeMonthlyDiariesUseCase: ObserveMonthlyDiariesUseCase

    @Before
    fun setUp() {
        diaryRepository = FakeDiaryRepository()
        settingsRepository = FakeSettingsRepository()
        observeMonthlyDiariesUseCase = ObserveMonthlyDiariesUseCase(
            diaryRepository = diaryRepository,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `saved cover is preferred and fallback uses latest diary photo when missing`() = runTest {
        val yearMonth = YearMonth.of(2026, 1)
        val dateWithCover = LocalDate.of(2026, 1, 5)
        val dateWithFallback = LocalDate.of(2026, 1, 6)
        val dateWithoutPhotos = LocalDate.of(2026, 1, 7)

        diaryRepository.addDiary(
            Diary(
                id = "d1",
                date = dateWithCover,
                title = "t1",
                content = "c1",
                createdAt = 1L,
                coverPhotoUri = "cover1"
            )
        )

        diaryRepository.addDiaryForDate(dateWithFallback, "t2", "c2", listOf("uri1", "uri2"))
        diaryRepository.addDiaryForDate(dateWithFallback, "t3", "c3", listOf("uri3"))
        diaryRepository.addDiaryForDate(dateWithoutPhotos, "t4", "c4")

        val result = observeMonthlyDiariesUseCase(yearMonth).first()

        assertEquals("cover1", result[dateWithCover])
        assertEquals("uri3", result[dateWithFallback])
        assertFalse(result.containsKey(dateWithoutPhotos))
    }

    @Test
    fun `emits empty then updates when diary is added`() = runTest {
        val yearMonth = YearMonth.of(2026, 1)
        val date = LocalDate.of(2026, 1, 2)
        val emissions = mutableListOf<Map<LocalDate, String>>()

        val job = launch {
            observeMonthlyDiariesUseCase(yearMonth)
                .take(2)
                .toList(emissions)
        }

        advanceUntilIdle()
        diaryRepository.addDiary(
            Diary(
                id = "d2",
                date = date,
                title = "t",
                content = "c",
                createdAt = 2L,
                coverPhotoUri = "cover2"
            )
        )
        advanceUntilIdle()
        job.join()

        assertEquals(true, emissions.first().isEmpty())
        assertEquals("cover2", emissions.last()[date])
    }
}
